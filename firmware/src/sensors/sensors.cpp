#include "sensors.h"
#include "../config/sensor_backend_config.h"
#include <Arduino.h>

#if defined(BIKEOS_REAR_SENSOR_VL53L1X)
    #include <Wire.h>
    #include <VL53L1X.h>
#endif

// ============================================================================
// Rear distance - two selectable backends (config/sensor_backend_config.h):
//
//   VL53L1X (I2C Time-of-Flight, original hardware)
//     Library: Pololu VL53L1X Arduino library.
//       PlatformIO: lib_deps = pololu/VL53L1X @ ^1.0.3 (see platformio.ini)
//     Wiring (standard ESP32 DevKit I2C pins - adjust if your board differs):
//       VIN -> 3.3V (check your breakout's silkscreen - do NOT assume 5V-safe)
//       GND -> GND, SDA -> GPIO 21, SCL -> GPIO 22
//
//   HC-SR04 (ultrasonic trigger/echo, Phase J alternate)
//     No library needed - trigger/echo GPIO + pulseIn() timing.
//     distance_mm = pulse_duration_us * speed_of_sound_mm_per_us / 2
//     (divide by 2 because pulseIn() measures the full round trip).
//     Wiring: VCC -> 5V (HC-SR04 is NOT 3.3V native - most breakouts need
//     5V to work reliably, but the ECHO pin's 5V output must be leveled
//     down to 3.3V for the ESP32 GPIO, e.g. a simple resistor divider or
//     level shifter - do NOT wire ECHO directly to an ESP32 GPIO at 5V).
//
// Both backends expose the exact same getRearDistanceMm()/isRearSensorReady()
// interface - nothing outside this file needs to know which is active.
//
// Wheel + Cadence - MH Sensor Series Hall modules, two selectable
// interfaces (config/sensor_backend_config.h):
//
//   DIGITAL (DO pin, interrupt-driven pulse counting, original hardware)
//     Open-drain/digital output. Wiring: VCC -> 3.3V, GND -> GND,
//     OUT -> the GPIOs defined below.
//
//   ANALOG (AO pin, polled + threshold-compared, Phase J alternate)
//     Some Hall modules only expose (or more reliably expose) an analog
//     AO pin instead of/alongside DO. No interrupt possible on an analog
//     read, so this is serviced by polling in poll() instead of an ISR -
//     poll() is called every main loop() iteration (no delay in loop()),
//     so this still catches pulses at effectively the same resolution as
//     the ISR path for any realistic wheel/crank RPM.
//
// Both Hall interfaces respect BIKEOS_HALL_ACTIVE_LOW/HIGH independently -
// i.e. AO mode can be active-high or active-low same as DO mode.
// ============================================================================

#define WHEEL_HALL_PIN 4
#define CADENCE_HALL_PIN 5

// How many magnets are mounted per wheel/crank rotation. 1 is the simplest
// setup (one magnet on the wheel, one on the crank arm). Increase this if
// multiple magnets are added later for better low-speed/low-cadence
// resolution - RPM math below divides by these automatically.
#define MAGNETS_PER_WHEEL 1
#define MAGNETS_PER_CRANK 1

// Ignore pulses closer together than this - filters contact bounce / sensor
// noise, not a real rider limit (this allows up to 2000 pulses/min per
// sensor, far beyond any real wheel/crank rate). Used by both the ISR
// (digital) and the polled-edge-detect (analog) paths.
#define PULSE_DEBOUNCE_MS 30

// RPM is recomputed once per window from the pulse count accumulated during
// it - shorter windows feel more "live" but are noisier at low cadence.
#define RPM_WINDOW_MS 1000

namespace bikeos::sensors {
namespace {
    // ---- Rear distance state (shared by both backends) ----
    bool rearSensorReady = false;
    uint16_t lastRearDistanceMm = 0;

#if defined(BIKEOS_REAR_SENSOR_VL53L1X)
    VL53L1X rearSensor;
    const uint32_t MEASUREMENT_TIMING_BUDGET_US = 50000;
    const uint16_t CONTINUOUS_PERIOD_MS = 50;

    void initRearSensor() {
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

    void pollRearSensor() {
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
#elif defined(BIKEOS_REAR_SENSOR_HC_SR04)
    // Speed of sound at ~20C, in mm per microsecond, halved in advance
    // since pulseIn() gives the round-trip time.
    const float MM_PER_US_ROUNDTRIP = 0.343f / 2.0f;

    // Space consecutive triggers apart - HC-SR04 datasheet recommends
    // >=60ms between measurements to let the previous echo fully settle
    // and avoid cross-talk with the next trigger.
    const unsigned long HCSR04_MEASURE_INTERVAL_MS = 60;
    unsigned long lastHcSr04MeasureMs = 0;

    void initRearSensor() {
        pinMode(HCSR04_TRIG_PIN, OUTPUT);
        pinMode(HCSR04_ECHO_PIN, INPUT);
        digitalWrite(HCSR04_TRIG_PIN, LOW);

        // No real "init failure" mode for HC-SR04 the way VL53L1X reports
        // one over I2C - it's just GPIO. Mark ready optimistically; a
        // genuinely unwired/dead sensor will just report 0 forever via
        // pulseIn() timing out below, which callers already treat as
        // "no data" per getRearDistanceMm()'s contract.
        rearSensorReady = true;
        Serial.printf("[Sensors] HC-SR04 ready (trig=GPIO%d, echo=GPIO%d)\n", HCSR04_TRIG_PIN, HCSR04_ECHO_PIN);
    }

    void pollRearSensor() {
        if (!rearSensorReady) return;
        unsigned long now = millis();
        if (now - lastHcSr04MeasureMs < HCSR04_MEASURE_INTERVAL_MS) return;
        lastHcSr04MeasureMs = now;

        // 10us HIGH trigger pulse per datasheet.
        digitalWrite(HCSR04_TRIG_PIN, LOW);
        delayMicroseconds(2);
        digitalWrite(HCSR04_TRIG_PIN, HIGH);
        delayMicroseconds(10);
        digitalWrite(HCSR04_TRIG_PIN, LOW);

        // pulseIn() blocks until ECHO goes HIGH-then-LOW or the timeout
        // elapses - bounded by HCSR04_PULSEIN_TIMEOUT_US so a disconnected/
        // dead sensor can't stall poll() for longer than that.
        unsigned long durationUs = pulseIn(HCSR04_ECHO_PIN, HIGH, HCSR04_PULSEIN_TIMEOUT_US);
        if (durationUs == 0) {
            // Timed out - no echo received (out of range or nothing to
            // reflect off). Leave lastRearDistanceMm at its previous value
            // rather than zeroing it, same "0 = no data yet" contract as
            // VL53L1X (0 only means "never got a reading at all").
            return;
        }

        lastRearDistanceMm = (uint16_t)((float)durationUs * MM_PER_US_ROUNDTRIP);
    }
#else
    #error "sensor_backend_config.h: exactly one BIKEOS_REAR_SENSOR_* backend must be defined"
#endif

    // ---- Hall sensor state ----
    // Written from ISR context (volatile) in DIGITAL mode, or from poll()'s
    // own edge-detection in ANALOG mode - read/reset with interrupts
    // briefly disabled around the read-and-reset in both cases (harmless
    // no-op overhead in ANALOG mode, but keeps one code path for both).
    volatile uint32_t wheelPulseCount = 0;
    volatile uint32_t cadencePulseCount = 0;
    volatile unsigned long lastWheelPulseMs = 0;
    volatile unsigned long lastCadencePulseMs = 0;

    uint16_t lastWheelRpm = 0;
    uint16_t lastCadenceRpm = 0;
    unsigned long rpmWindowStartMs = 0;

#if defined(BIKEOS_HALL_MODE_DIGITAL)
    #if defined(BIKEOS_HALL_ACTIVE_HIGH)
        #define HALL_ISR_EDGE RISING
    #else
        #define HALL_ISR_EDGE FALLING
    #endif

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

    void initHallSensors() {
        pinMode(WHEEL_HALL_PIN, INPUT_PULLUP);
        pinMode(CADENCE_HALL_PIN, INPUT_PULLUP);
        attachInterrupt(digitalPinToInterrupt(WHEEL_HALL_PIN), onWheelPulse, HALL_ISR_EDGE);
        attachInterrupt(digitalPinToInterrupt(CADENCE_HALL_PIN), onCadencePulse, HALL_ISR_EDGE);
        rpmWindowStartMs = millis();
        Serial.printf("[Sensors] Hall ISRs attached (wheel=GPIO%d, cadence=GPIO%d, digital, %s)\n",
            WHEEL_HALL_PIN, CADENCE_HALL_PIN,
    #if defined(BIKEOS_HALL_ACTIVE_HIGH)
            "active-HIGH"
    #else
            "active-LOW"
    #endif
        );
    }

    // No polling needed for pulse capture in digital mode - the ISR does
    // it. Kept as a no-op so main poll() below can call it unconditionally
    // regardless of which Hall mode is active.
    void pollHallEdges() {}
#elif defined(BIKEOS_HALL_MODE_ANALOG)
    // Tracks whether each channel was last read "active" (magnet present)
    // so a transition into that state can be detected as one pulse -
    // mirrors what the digital ISR's FALLING/RISING edge trigger does,
    // just via polling instead of hardware interrupt.
    bool wheelWasActive = false;
    bool cadenceWasActive = false;

    inline bool isActive(int rawAdc) {
    #if defined(BIKEOS_HALL_ACTIVE_HIGH)
        return rawAdc >= HALL_AO_THRESHOLD;
    #else
        return rawAdc <= HALL_AO_THRESHOLD;
    #endif
    }

    void initHallSensors() {
        pinMode(WHEEL_HALL_AO_PIN, INPUT);
        pinMode(CADENCE_HALL_AO_PIN, INPUT);
        rpmWindowStartMs = millis();
        Serial.printf("[Sensors] Hall AO polling ready (wheel=GPIO%d, cadence=GPIO%d, analog, threshold=%d, %s)\n",
            WHEEL_HALL_AO_PIN, CADENCE_HALL_AO_PIN, HALL_AO_THRESHOLD,
    #if defined(BIKEOS_HALL_ACTIVE_HIGH)
            "active-HIGH"
    #else
            "active-LOW"
    #endif
        );
    }

    // Called every poll() (i.e. every loop() iteration) - reads both AO
    // pins and detects "just became active" transitions, applying the
    // same debounce window the digital ISR path uses so a magnet
    // lingering near the sensor doesn't produce a flood of pulses.
    void pollHallEdges() {
        unsigned long now = millis();

        bool wheelActive = isActive(analogRead(WHEEL_HALL_AO_PIN));
        if (wheelActive && !wheelWasActive && (now - lastWheelPulseMs >= PULSE_DEBOUNCE_MS)) {
            lastWheelPulseMs = now;
            wheelPulseCount++;
        }
        wheelWasActive = wheelActive;

        bool cadenceActive = isActive(analogRead(CADENCE_HALL_AO_PIN));
        if (cadenceActive && !cadenceWasActive && (now - lastCadencePulseMs >= PULSE_DEBOUNCE_MS)) {
            lastCadencePulseMs = now;
            cadencePulseCount++;
        }
        cadenceWasActive = cadenceActive;
    }
#else
    #error "sensor_backend_config.h: exactly one BIKEOS_HALL_MODE_* interface must be defined"
#endif

    void pollHallSensors() {
        pollHallEdges();

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
    initRearSensor();
    initHallSensors();
}

void poll() {
    pollRearSensor();
    pollHallSensors();
}

uint16_t getRearDistanceMm() { return lastRearDistanceMm; }
bool isRearSensorReady() { return rearSensorReady; }
uint16_t getWheelRpm() { return lastWheelRpm; }
uint16_t getCadenceRpm() { return lastCadenceRpm; }
unsigned long getLastWheelPulseMs() { return lastWheelPulseMs; }

} // namespace bikeos::sensors
