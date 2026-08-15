#include "alarm.h"
#include <Arduino.h>
#include <math.h>
#include "../sensors/sensors.h"
#include "../motion/motion.h"
#include "../controls/controls.h"
#include "../buzzer/buzzer.h"

// ============================================================================
// Buzzer wiring/pin ownership moved to buzzer.h/buzzer.cpp (Phase H's
// keyless-starter ignition chime needed the same GPIO for short one-shot
// beep patterns, so it's now a shared module instead of alarm.cpp owning
// the pin outright) - this module now goes through bikeos::buzzer::drive()
// instead of digitalWrite() directly.
// ============================================================================

#define ARM_GRACE_PERIOD_MS 3000UL   // ignore triggers right after arming - avoids false-triggering on the arming action itself
#define MOTION_WINDOW_MS 7000UL      // per the spec: compare accel average across a ~7s window
#define MOTION_DELTA_THRESHOLD_G 0.35f // tuned conservatively - revisit after real-world testing on the actual bike
#define BLINK_INTERVAL_MS 300UL

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
            bikeos::buzzer::drive(blinkOn);
        }
    }

    void silenceOutputs() {
        bikeos::buzzer::drive(false);
        // Deliberately NOT forcing lights off here - if the rider had them
        // on manually before the alarm triggered, disarm() should restore
        // that state, not silently turn lights off. Simplest correct
        // behavior: leave whatever the blink loop last set; the next
        // manual light toggle (button or app) takes over from there.
    }
}

void init() {
    // Buzzer pin setup now lives in buzzer::init() (called from main.cpp) -
    // this module no longer touches pinMode()/digitalWrite() directly.
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
        bikeos::buzzer::lockForExternalControl(true);
        Serial.println("[Alarm] Triggered - wheel movement detected while armed");
        return;
    }

    checkMotionWindow();
    if (triggered) {
        bikeos::buzzer::lockForExternalControl(true);
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
    bikeos::buzzer::lockForExternalControl(false);
    Serial.println("[Alarm] Disarmed");
}

bool isArmed() { return armed; }
bool isTriggered() { return triggered; }

} // namespace bikeos::alarm
