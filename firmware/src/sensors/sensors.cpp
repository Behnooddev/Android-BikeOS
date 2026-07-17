#include "sensors.h"
#include <Arduino.h>
#include <Wire.h>
#include <VL53L1X.h>

// ============================================================================
// Rear distance - VL53L1X (I2C Time-of-Flight)
//   Library: Pololu VL53L1X Arduino library.
//     PlatformIO: lib_deps = pololu/VL53L1X @ ^1.0.3 (see platformio.ini)
//   Wiring (standard ESP32 DevKit I2C pins - adjust if your board differs):
//     VIN -> 3.3V (check your breakout's silkscreen - do NOT assume 5V-safe)
//     GND -> GND, SDA -> GPIO 21, SCL -> GPIO 22
//
// Wheel + Cadence - MH Sensor Series digital Hall modules
//   Open-drain/digital output, active LOW when a magnet is present (typical
//   for these breakout boards - confirm with a multimeter if pulses aren't
//   registering; some variants are active HIGH and would need FALLING
//   changed to RISING below).
//   Wiring: VCC -> 3.3V, GND -> GND, OUT -> the GPIOs defined below.
// ============================================================================

#define WHEEL_HALL_PIN 4
#define CADENCE_HALL_PIN 5

#define STR_HELPER(x) #x
#define STR(x) STR_HELPER(x)

// How many magnets are mounted per wheel/crank rotation. 1 is the simplest
// setup (one magnet on the wheel, one on the crank arm). Increase this if
// multiple magnets are added later for better low-speed/low-cadence
// resolution - RPM math below divides by these automatically.
#define MAGNETS_PER_WHEEL 1
#define MAGNETS_PER_CRANK 1

// Ignore pulses closer together than this - filters contact bounce / sensor
// noise, not a real rider limit (this allows up up to 2000 pulses/min per
// sensor, far beyond any real wheel/crank rate).
#define PULSE_DEBOUNCE_MS 30

// RPM is recomputed once per window from the pulse count accumulated during
// it - shorter windows feel more "live" but are noisier at low cadence.
#define RPM_WINDOW_MS 1000

namespace bikeos::sensors {
namespace {
    // ---- VL53L1X state ----
    VL53L1X rearSensor;
    bool rearSensorReady = false;
    uint16_t lastRearDistanceMm = 0;
    const uint32_t MEASUREMENT_TIMING_BUDGET_US = 50000;
    const uint16_t CONTINUOUS_PERIOD_MS = 50;

    // ---- Hall sensor state ----
    // Written from ISR context (volatile), read/reset from poll() with
    // interrupts briefly disabled around the read-and-reset to avoid a
    // torn read racing with an ISR incrementing the same counter.
    volatile uint32_t wheelPulseCount = 0;
    volatile uint32_t cadencePulseCount = 0;
    volatile unsigned long lastWheelPulseMs = 0;
    volatile unsigned long lastCadencePulseMs = 0;

    uint16_t lastWheelRpm = 0;
    uint16_t lastCadenceRpm = 0;
    unsigned long rpmWindowStartMs = 0;

    void IRAM_ATTR onWheelPulse() {
        unsigned long now = millis();
        if (now - lastWheelPulseMs < PULSE_DEBOUNCE_MS) return;
        lastWheelPulseMs = now;
        wheelPulseCount++;
    }

    void IRAM_ATTR onCadencePulse() {
        unsigned long now = millis();
        if (now - lastCadencePulseMs < PULSE_DEBOUNCE_MS) return;
        lastCadencePulseMs = now;
        cadencePulseCount++;
    }

    void initVl53l1x() {
        Wire.begin();
        Wire.setClock(400000);

        rearSensor.setTimeout(500);
        if (!rearSensor.init()) {
            Serial.println("[Sensors] VL53L1X init FAILED - check wiring/address");
            rearSensorReady = false;
            return;
        }

        // Long range mode: better for a rear-approach-warning use case (up
        // to ~4m) than Short/Medium, at the cost of being more sensitive to
        // ambient light. Revisit if outdoor sunlight causes false readings.
        rearSensor.setDistanceMode(VL53L1X::Long);
        rearSensor.setMeasurementTimingBudget(MEASUREMENT_TIMING_BUDGET_US);
        rearSensor.startContinuous(CONTINUOUS_PERIOD_MS);

        rearSensorReady = true;
        Serial.println("[Sensors] VL53L1X ready");
    }

    void initHallSensors() {
        pinMode(WHEEL_HALL_PIN, INPUT_PULLUP);
        pinMode(CADENCE_HALL_PIN, INPUT_PULLUP);
        attachInterrupt(digitalPinToInterrupt(WHEEL_HALL_PIN), onWheelPulse, FALLING);
        attachInterrupt(digitalPinToInterrupt(CADENCE_HALL_PIN), onCadencePulse, FALLING);
        rpmWindowStartMs = millis();
        Serial.printf("[Sensors] Hall ISRs attached (wheel=GPIO%d, cadence=GPIO%d)\n", WHEEL_HALL_PIN, CADENCE_HALL_PIN);
    }

    void pollVl53l1x() {
        if (!rearSensorReady) return;
        // dataReady() is non-blocking - only calls read() when a fresh
        // sample is actually available, so this never stalls loop() the
        // way rearSensor.read() alone (which blocks until timeout) would.
        if (!rearSensor.dataReady()) return;

        uint16_t distance = rearSensor.read(false);
        if (rearSensor.timeoutOccurred()) {
            Serial.println("[Sensors] VL53L1X read timeout");
            return;
        }
        lastRearDistanceMm = distance;
    }

    void pollHallSensors() {
        unsigned long now = millis();
        if (now - rpmWindowStartMs < RPM_WINDOW_MS) return;

        noInterrupts();
        uint32_t wheelPulses = wheelPulseCount;
        uint32_t cadencePulses = cadencePulseCount;
        wheelPulseCount = 0;
        cadencePulseCount = 0;
        interrupts();

        float windowMinutes = (float)(now - rpmWindowStartMs) / 60000.0f;
        lastWheelRpm = (uint16_t)((wheelPulses / (float)MAGNETS_PER_WHEEL) / windowMinutes);
        lastCadenceRpm = (uint16_t)((cadencePulses / (float)MAGNETS_PER_CRANK) / windowMinutes);

        rpmWindowStartMs = now;
    }
}

void init() {
    initVl53l1x();
    initHallSensors();
}

void poll() {
    pollVl53l1x();
    pollHallSensors();
}

uint16_t getRearDistanceMm() { return lastRearDistanceMm; }
bool isRearSensorReady() { return rearSensorReady; }
uint16_t getWheelRpm() { return lastWheelRpm; }
uint16_t getCadenceRpm() { return lastCadenceRpm; }

} // namespace bikeos::sensors
