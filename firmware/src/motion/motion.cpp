#include "motion.h"
#include "../config/sensor_backend_config.h"
#include <Arduino.h>
#include <Wire.h>

#if defined(BIKEOS_MPU6050_LIBRARY)
    #include <Adafruit_MPU6050.h>
    #include <Adafruit_Sensor.h>
#endif

// Two selectable reading paths (config/sensor_backend_config.h):
//
//   LIBRARY (Adafruit MPU6050, original hardware)
//     Library: Adafruit MPU6050 (+ its Adafruit Sensor + Adafruit BusIO deps).
//       PlatformIO: lib_deps = adafruit/Adafruit MPU6050 @ ^2.2.6 (see platformio.ini)
//     Gives calibrated/scaled acceleration directly - simplest path, trusts
//     the library's handling of whatever's actually on the chip.
//
//   RAW_AVERAGING (Phase J alternate, no Adafruit MPU6050 dependency)
//     The builder suspects some MPU6050 units may be counterfeit/inaccurate.
//     This path talks to the chip directly over I2C (raw register reads),
//     bypassing the library entirely, and applies a simple rolling average
//     over the last MPU6050_RAW_AVERAGE_WINDOW samples before computing the
//     magnitude - smooths out the kind of single-sample noise spikes that
//     cheap/fake accelerometer dies are prone to, without trusting any
//     library-side calibration that a clone chip might not actually honor.
//     Uses the chip's default power-on range (+/-2g, AFS_SEL=0,
//     16384 LSB/g) rather than reconfiguring ACCEL_CONFIG - deliberately
//     minimal register writes (only PWR_MGMT_1 to wake the chip) to reduce
//     the surface area of things a clone chip could get wrong.
//
// Both paths share the same wiring (I2C bus already set up in sensors.cpp:
// SDA=GPIO21, SCL=GPIO22) and I2C address (MPU6050 default 0x68) - only the
// software path differs.

#define SAMPLE_INTERVAL_MS 100 // 10Hz - plenty for "did something jostle the bike", not motion-controller-grade

namespace bikeos::motion {
namespace {
    bool ready = false;
    float lastAccelMagnitudeG = 1.0f; // ~1g at rest (gravity)
    unsigned long lastSampleMs = 0;

#if defined(BIKEOS_MPU6050_LIBRARY)
    Adafruit_MPU6050 mpu;

    void initImpl() {
        if (!mpu.begin()) {
            Serial.println("[Motion] MPU6050 init FAILED - check wiring/address");
            ready = false;
            return;
        }

        mpu.setAccelerometerRange(MPU6050_RANGE_4_G);
        mpu.setGyroRange(MPU6050_RANGE_500_DEG);
        mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

        ready = true;
        Serial.println("[Motion] MPU6050 ready (library, calibrated)");
    }

    void sampleImpl() {
        sensors_event_t accel, gyro, temp;
        mpu.getEvent(&accel, &gyro, &temp);

        // m/s^2 -> g, then magnitude of the 3-axis vector.
        const float gx = accel.acceleration.x / 9.80665f;
        const float gy = accel.acceleration.y / 9.80665f;
        const float gz = accel.acceleration.z / 9.80665f;
        lastAccelMagnitudeG = sqrtf(gx * gx + gy * gy + gz * gz);
    }
#elif defined(BIKEOS_MPU6050_RAW_AVERAGING)
    #define MPU6050_I2C_ADDR      0x68
    #define MPU6050_REG_PWR_MGMT1 0x6B
    #define MPU6050_REG_WHO_AM_I  0x75
    #define MPU6050_REG_ACCEL_XOUT_H 0x3B
    // Default power-on sensitivity at AFS_SEL=0 (+/-2g) - see comment above
    // on why this path deliberately doesn't touch ACCEL_CONFIG.
    const float RAW_LSB_PER_G = 16384.0f;

    int16_t xBuf[MPU6050_RAW_AVERAGE_WINDOW] = {0};
    int16_t yBuf[MPU6050_RAW_AVERAGE_WINDOW] = {0};
    int16_t zBuf[MPU6050_RAW_AVERAGE_WINDOW] = {0};
    uint8_t bufIndex = 0;
    uint8_t samplesFilled = 0;

    bool writeRegister(uint8_t reg, uint8_t value) {
        Wire.beginTransmission(MPU6050_I2C_ADDR);
        Wire.write(reg);
        Wire.write(value);
        return Wire.endTransmission() == 0;
    }

    // Reads 6 bytes starting at ACCEL_XOUT_H: XH XL YH YL ZH ZL, each pair
    // big-endian two's-complement. Returns false on I2C error (e.g. chip
    // not responding - a torn/absent connection on a clone board).
    bool readRawAccel(int16_t& x, int16_t& y, int16_t& z) {
        Wire.beginTransmission(MPU6050_I2C_ADDR);
        Wire.write(MPU6050_REG_ACCEL_XOUT_H);
        if (Wire.endTransmission(false) != 0) return false;

        if (Wire.requestFrom((uint8_t)MPU6050_I2C_ADDR, (uint8_t)6) != 6) return false;

        x = (int16_t)((Wire.read() << 8) | Wire.read());
        y = (int16_t)((Wire.read() << 8) | Wire.read());
        z = (int16_t)((Wire.read() << 8) | Wire.read());
        return true;
    }

    void initImpl() {
        Wire.beginTransmission(MPU6050_I2C_ADDR);
        if (Wire.endTransmission() != 0) {
            Serial.println("[Motion] MPU6050 (raw) not found on I2C bus - check wiring/address");
            ready = false;
            return;
        }

        // PWR_MGMT_1 defaults to sleep-mode-on-reset on real MPU6050 chips;
        // clones sometimes power up already awake, but writing 0x00
        // (wake, internal 8MHz oscillator) is harmless either way.
        if (!writeRegister(MPU6050_REG_PWR_MGMT1, 0x00)) {
            Serial.println("[Motion] MPU6050 (raw) wake write FAILED");
            ready = false;
            return;
        }
        delay(50); // let the chip stabilize after waking, matches library behavior

        bufIndex = 0;
        samplesFilled = 0;
        ready = true;
        Serial.println("[Motion] MPU6050 ready (raw register + rolling average)");
    }

    void sampleImpl() {
        int16_t x, y, z;
        if (!readRawAccel(x, y, z)) {
            // Leave lastAccelMagnitudeG at its previous value on a transient
            // I2C read failure rather than injecting a garbage 0 sample into
            // the rolling average.
            return;
        }

        xBuf[bufIndex] = x;
        yBuf[bufIndex] = y;
        zBuf[bufIndex] = z;
        bufIndex = (bufIndex + 1) % MPU6050_RAW_AVERAGE_WINDOW;
        if (samplesFilled < MPU6050_RAW_AVERAGE_WINDOW) samplesFilled++;

        long sumX = 0, sumY = 0, sumZ = 0;
        for (uint8_t i = 0; i < samplesFilled; i++) {
            sumX += xBuf[i];
            sumY += yBuf[i];
            sumZ += zBuf[i];
        }
        const float avgX = (float)sumX / samplesFilled;
        const float avgY = (float)sumY / samplesFilled;
        const float avgZ = (float)sumZ / samplesFilled;

        const float gx = avgX / RAW_LSB_PER_G;
        const float gy = avgY / RAW_LSB_PER_G;
        const float gz = avgZ / RAW_LSB_PER_G;
        lastAccelMagnitudeG = sqrtf(gx * gx + gy * gy + gz * gz);
    }
#else
    #error "sensor_backend_config.h: exactly one BIKEOS_MPU6050_* path must be defined"
#endif
}

void init() {
    Wire.begin(); // idempotent if sensors::init() already called it
    initImpl();
    lastSampleMs = millis();
}

void poll() {
    if (!ready) return;
    if (millis() - lastSampleMs < SAMPLE_INTERVAL_MS) return;
    lastSampleMs = millis();

    sampleImpl();
}

float getAccelMagnitude() { return lastAccelMagnitudeG; }
bool isReady() { return ready; }

} // namespace bikeos::motion
