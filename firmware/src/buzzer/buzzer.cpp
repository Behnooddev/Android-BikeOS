#include "buzzer.h"
#include <Arduino.h>

// ============================================================================
// Wiring: buzzer OUT -> GPIO23 (through a transistor/MOSFET if it's not a
// small active buzzer module that can be driven directly from a GPIO -
// check your specific buzzer's current draw; passive piezo buzzers under
// ~20mA are usually fine straight off a GPIO, anything louder needs a
// switching transistor same as the lights). Unchanged from when alarm.cpp
// owned this pin directly - only the software ownership moved.
// ============================================================================

#define BUZZER_PIN 23

namespace bikeos::buzzer {
namespace {
    bool locked = false;

    bool patternActive = false;
    uint8_t beepsRemaining = 0;
    bool beepOn = false;
    unsigned long lastToggleMs = 0;
    unsigned long onDurationMs = 120;
    unsigned long offDurationMs = 120;
}

void init() {
    pinMode(BUZZER_PIN, OUTPUT);
    digitalWrite(BUZZER_PIN, LOW);
}

void drive(bool on) {
    digitalWrite(BUZZER_PIN, on ? HIGH : LOW);
}

void lockForExternalControl(bool isLocked) {
    locked = isLocked;
    if (locked) {
        // An external owner (alarm.cpp) is taking over - abandon any
        // in-progress beepPattern() so its next poll() doesn't stomp on
        // whatever alarm.cpp just set.
        patternActive = false;
    }
}

void beepPattern(uint8_t count, unsigned long onMs, unsigned long offMs) {
    if (locked || patternActive || count == 0) return;

    patternActive = true;
    beepsRemaining = count;
    onDurationMs = onMs;
    offDurationMs = offMs;

    beepOn = true;
    digitalWrite(BUZZER_PIN, HIGH);
    lastToggleMs = millis();
}

void poll() {
    if (locked || !patternActive) return;

    unsigned long now = millis();
    unsigned long elapsed = now - lastToggleMs;

    if (beepOn) {
        if (elapsed >= onDurationMs) {
            digitalWrite(BUZZER_PIN, LOW);
            beepOn = false;
            lastToggleMs = now;
            beepsRemaining--;
            if (beepsRemaining == 0) patternActive = false;
        }
    } else if (elapsed >= offDurationMs) {
        digitalWrite(BUZZER_PIN, HIGH);
        beepOn = true;
        lastToggleMs = now;
    }
}

} // namespace bikeos::buzzer
