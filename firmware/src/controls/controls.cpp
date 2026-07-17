#include "controls.h"
#include <Arduino.h>

// ============================================================================
// Wiring
//   Lights (logic-level MOSFET gates - GPIO drives the gate directly,
//   MOSFET switches the actual light load from the main battery, NOT
//   powered from the GPIO pin itself):
//     FRONT_LIGHT_PIN -> front MOSFET gate
//     REAR_LIGHT_PIN  -> rear MOSFET gate
//     BODY_LIGHT_PIN  -> body MOSFET gate
//
//   Buttons (active LOW, internal pull-up - one leg to GPIO, other leg to GND):
//     LIGHT_BUTTON_PIN     -> local light-cycle toggle
//     MODE_BUTTON_PIN      -> ride mode change (Android-side)
//     GEAR_UP_BUTTON_PIN   -> gear up (Android-side)
//     GEAR_DOWN_BUTTON_PIN -> gear down (Android-side)
// ============================================================================

#define FRONT_LIGHT_PIN 13
#define REAR_LIGHT_PIN  14
#define BODY_LIGHT_PIN  27

#define LIGHT_BUTTON_PIN     32
#define MODE_BUTTON_PIN      33
#define GEAR_UP_BUTTON_PIN   25
#define GEAR_DOWN_BUTTON_PIN 26

#define BUTTON_DEBOUNCE_MS 250 // generous on purpose - these are deliberate rider taps, not sensor pulses

namespace bikeos::controls {
namespace {
    bool frontLightOn = false;
    bool rearLightOn = false;
    bool bodyLightOn = false;

    // Local light-cycle state: 0 = off, 1 = front+rear, 2 = front+rear+body
    uint8_t lightCycleStep = 0;

    struct ButtonState {
        uint8_t pin;
        bool lastReading = HIGH;   // idle level with INPUT_PULLUP
        unsigned long lastChangeMs = 0;
        bool pendingPress = false; // latched until consumed
        explicit ButtonState(uint8_t p) : pin(p) {}
    };

    ButtonState lightButton(LIGHT_BUTTON_PIN);
    ButtonState modeButton(MODE_BUTTON_PIN);
    ButtonState gearUpButton(GEAR_UP_BUTTON_PIN);
    ButtonState gearDownButton(GEAR_DOWN_BUTTON_PIN);

    void applyLightOutputs() {
        digitalWrite(FRONT_LIGHT_PIN, frontLightOn ? HIGH : LOW);
        digitalWrite(REAR_LIGHT_PIN, rearLightOn ? HIGH : LOW);
        digitalWrite(BODY_LIGHT_PIN, bodyLightOn ? HIGH : LOW);
    }

    void advanceLightCycle() {
        lightCycleStep = (lightCycleStep + 1) % 3;
        switch (lightCycleStep) {
            case 0: frontLightOn = false; rearLightOn = false; bodyLightOn = false; break;
            case 1: frontLightOn = true;  rearLightOn = true;  bodyLightOn = false; break;
            case 2: frontLightOn = true;  rearLightOn = true;  bodyLightOn = true;  break;
        }
        applyLightOutputs();
        Serial.printf("[Controls] Light cycle -> step %d\n", lightCycleStep);
    }

    /** True exactly once, on the debounced press edge (idle -> pressed). */
    bool detectPressEdge(ButtonState& button) {
        bool reading = digitalRead(button.pin);
        unsigned long now = millis();

        if (reading != button.lastReading && (now - button.lastChangeMs) > BUTTON_DEBOUNCE_MS) {
            button.lastChangeMs = now;
            bool wasIdle = (button.lastReading == HIGH);
            button.lastReading = reading;
            if (wasIdle && reading == LOW) { // active LOW: just pressed
                return true;
            }
        }
        return false;
    }
}

void init() {
    pinMode(FRONT_LIGHT_PIN, OUTPUT);
    pinMode(REAR_LIGHT_PIN, OUTPUT);
    pinMode(BODY_LIGHT_PIN, OUTPUT);
    applyLightOutputs(); // lights start OFF

    pinMode(LIGHT_BUTTON_PIN, INPUT_PULLUP);
    pinMode(MODE_BUTTON_PIN, INPUT_PULLUP);
    pinMode(GEAR_UP_BUTTON_PIN, INPUT_PULLUP);
    pinMode(GEAR_DOWN_BUTTON_PIN, INPUT_PULLUP);

    Serial.println("[Controls] Lights + buttons ready");
}

void poll() {
    // Light button acts immediately and locally - no persistent latch needed.
    if (detectPressEdge(lightButton)) advanceLightCycle();

    // Mode/Gear buttons latch a pending flag; main.cpp's loop() drains
    // these via consumeModeButtonPress()/consumeGearUpButtonPress()/
    // consumeGearDownButtonPress() and forwards them to Android over BLE.
    if (detectPressEdge(modeButton)) modeButton.pendingPress = true;
    if (detectPressEdge(gearUpButton)) gearUpButton.pendingPress = true;
    if (detectPressEdge(gearDownButton)) gearDownButton.pendingPress = true;
}

void setFrontLight(bool on) { frontLightOn = on; applyLightOutputs(); }
void setRearLight(bool on) { rearLightOn = on; applyLightOutputs(); }
void setBodyLight(bool on) { bodyLightOn = on; applyLightOutputs(); }
bool isFrontLightOn() { return frontLightOn; }
bool isRearLightOn() { return rearLightOn; }
bool isBodyLightOn() { return bodyLightOn; }

bool consumeModeButtonPress() {
    if (modeButton.pendingPress) { modeButton.pendingPress = false; return true; }
    return false;
}

bool consumeGearUpButtonPress() {
    if (gearUpButton.pendingPress) { gearUpButton.pendingPress = false; return true; }
    return false;
}

bool consumeGearDownButtonPress() {
    if (gearDownButton.pendingPress) { gearDownButton.pendingPress = false; return true; }
    return false;
}

} // namespace bikeos::controls
