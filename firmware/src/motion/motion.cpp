#include "motion.h"
#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>

// Library: Adafruit MPU6050 (+ its Adafruit Sensor + Adafruit BusIO deps).
//   PlatformIO: lib_deps = adafruit/Adafruit MPU6050 @ ^2.2.6 (see platformio.ini)
//   Wiring: shares the I2C bus already set up in sensors.cpp (SDA=GPIO21,
//   SCL=GPIO22) alongside the VL53L1X and INA219 - all three coexist fine,
//   different I2C addresses (MPU6050 default 0x68).

#define SAMPLE_INTERVAL_MS 100 // 10Hz - plenty for "did something jostle the bike", not motion-controller-grade

namespace bikeos::motion {
namespace {
    Adafruit_MPU6050 mpu;
    bool ready = false;
    float lastAccelMagnitudeG = 1.0f; // ~1g at rest (gravity)
    unsigned long lastSampleMs = 0;
}

void init() {
    Wire.begin(); // idempotent if sensors::init() already called it

    if (!mpu.begin()) {
        Serial.println("[Motion] MPU6050 init FAILED - check wiring/address");
        ready = false;
        return;
    }

    mpu.setAccelerometerRange(MPU6050_RANGE_4_G);
    mpu.setGyroRange(MPU6050_RANGE_500_DEG);
    mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

    ready = true;
    lastSampleMs = millis();
    Serial.println("[Motion] MPU6050 ready");
}

void poll() {
    if (!ready) return;
    if (millis() - lastSampleMs < SAMPLE_INTERVAL_MS) return;
    lastSampleMs = millis();

    sensors_event_t accel, gyro, temp;
    mpu.getEvent(&accel, &gyro, &temp);

    // m/s^2 -> g, then magnitude of the 3-axis vector.
    const float gx = accel.acceleration.x / 9.80665f;
    const float gy = accel.acceleration.y / 9.80665f;
    const float gz = accel.acceleration.z / 9.80665f;
    lastAccelMagnitudeG = sqrtf(gx * gx + gy * gy + gz * gz);
}

float getAccelMagnitude() { return lastAccelMagnitudeG; }
bool isReady() { return ready; }

} // namespace bikeos::motion
