#include "alarm.h"
#include <Arduino.h>
#include <math.h>
#include "../sensors/sensors.h"
#include "../motion/motion.h"
#include "../controls/controls.h"

// ============================================================================
// Wiring: buzzer OUT -> GPIO23 (through a transistor/MOSFET if it's not a
// small active buzzer module that can be driven directly from a GPIO -
// check your specific buzzer's current draw; passive piezo buzzers under
// ~20mA are usually fine straight off a GPIO, anything louder needs a
// switching transistor same as the lights).
// ============================================================================

#define BUZZER_PIN 23

#define ARM_GRACE_PERIOD_MS 3000UL   // ignore triggers right after arming - avoids false-triggering on the arming action itself
#define MOTION_WINDOW_MS 7000UL      // per the spec: compare accel average across a ~7s window
#define MOTION_DELTA_THRESHOLD_G 0.35f // tuned conservatively - revisit after real-world testing on the actual bike
#define BLINK_INTERVAL_MS 300UL
#define BUZZER_BEEP_MS 200UL

namespace bikeos::alarm {
namespace {
    bool armed = false;
    bool triggered = false;
    unsigned long armedAtMs = 0;

    // Motion-window trigger state (mirrors the RPM-windowing pattern in sensors.cpp).
    float motionSampleSum = 0.0f;
    uint32_t motionSampleCount = 0;
    unsigned long motionWindowStartMs = 0;
    float previousWindowAvgG = 1.0f; // ~1g at rest

    // Blink/buzz pattern state while triggered.
    unsigned long lastBlinkToggleMs = 0;
    bool blinkOn = false;
    unsigned long lastBuzzMs = 0;

    void resetMotionWindow() {
        motionSampleSum = 0.0f;
        motionSampleCount = 0;
        motionWindowStartMs = millis();
    }

    bool wheelMovedSinceArming() {
        unsigned long lastPulse = bikeos::sensors::getLastWheelPulseMs();
        return lastPulse != 0 && lastPulse > armedAtMs;
    }

    void checkMotionWindow() {
        if (!bikeos::motion::isReady()) return;

        motionSampleSum += bikeos::motion::getAccelMagnitude();
        motionSampleCount++;

        if (millis() - motionWindowStartMs < MOTION_WINDOW_MS) return;
        if (motionSampleCount == 0) { resetMotionWindow(); return; }

        float windowAvgG = motionSampleSum / (float)motionSampleCount;
        float delta = fabsf(windowAvgG - previousWindowAvgG);

        if (delta > MOTION_DELTA_THRESHOLD_G) {
            triggered = true;
        }

        previousWindowAvgG = windowAvgG;
        resetMotionWindow();
    }

    void driveBuzzAndBlink() {
        unsigned long now = millis();

        if (now - lastBlinkToggleMs >= BLINK_INTERVAL_MS) {
            lastBlinkToggleMs = now;
            blinkOn = !blinkOn;
            bikeos::controls::setFrontLight(blinkOn);
            bikeos::controls::setRearLight(blinkOn);
        }

        if (now - lastBuzzMs >= BLINK_INTERVAL_MS) {
            lastBuzzMs = now;
            if (blinkOn) {
                digitalWrite(BUZZER_PIN, HIGH);
            } else {
                digitalWrite(BUZZER_PIN, LOW);
            }
        }
    }

    void silenceOutputs() {
        digitalWrite(BUZZER_PIN, LOW);
        // Deliberately NOT forcing lights off here - if the rider had them
        // on manually before the alarm triggered, disarm() should restore
        // that state, not silently turn lights off. Simplest correct
        // behavior: leave whatever the blink loop last set; the next
        // manual light toggle (button or app) takes over from there.
    }
}

void init() {
    pinMode(BUZZER_PIN, OUTPUT);
    digitalWrite(BUZZER_PIN, LOW);
}

void poll() {
    if (!armed) return;

    if (triggered) {
        driveBuzzAndBlink();
        return;
    }

    if (millis() - armedAtMs < ARM_GRACE_PERIOD_MS) return; // still in grace period

    if (wheelMovedSinceArming()) {
        triggered = true;
        Serial.println("[Alarm] Triggered - wheel movement detected while armed");
        return;
    }

    checkMotionWindow();
    if (triggered) {
        Serial.println("[Alarm] Triggered - motion delta exceeded threshold");
    }
}

void arm() {
    armed = true;
    triggered = false;
    armedAtMs = millis();
    resetMotionWindow();
    previousWindowAvgG = bikeos::motion::isReady() ? bikeos::motion::getAccelMagnitude() : 1.0f;
    Serial.println("[Alarm] Armed");
}

void disarm() {
    armed = false;
    triggered = false;
    silenceOutputs();
    Serial.println("[Alarm] Disarmed");
}

bool isArmed() { return armed; }
bool isTriggered() { return triggered; }

} // namespace bikeos::alarm
