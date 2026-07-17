#pragma once
// Handlebar buttons + MOSFET lighting outputs - Phase 4, real hardware
// (builder-confirmed: lights + buttons purchased).
//
// Lights can be driven from two independent paths that both just call the
// same setters below, last-write-wins:
//   1. The physical Light button (local, instant, works even if the phone
//      is disconnected - cycles Off -> Front+Rear -> Front+Rear+Body -> Off)
//   2. The app's Control Service commands (FRONT_LIGHT_ON/OFF etc,
//      handled in ble_service.cpp's ControlCommandCallbacks)
//
// Mode/Gear buttons do NOT change local firmware state - ride mode and
// gear counts are Android/Room-owned data the firmware doesn't have. These
// buttons instead raise an event that ble_service.cpp forwards to Android
// over BLE (see BIKEOS_MSG_TYPE_BUTTON_EVENT); Android decides what the
// next mode/gear actually is and is the single source of truth for both.

#include <cstdint>

namespace bikeos::controls {
    void init();
    void poll(); // debounces buttons, drives the local light-cycle behavior

    void setFrontLight(bool on);
    void setRearLight(bool on);
    void setBodyLight(bool on);
    bool isFrontLightOn();
    bool isRearLightOn();
    bool isBodyLightOn();

    /** Edge-triggered, test-and-clear: true once per press, then resets.
     *  Call once per loop() iteration per button - if you need the same
     *  press observed in two places, cache the result yourself. */
    bool consumeModeButtonPress();
    bool consumeGearUpButtonPress();
    bool consumeGearDownButtonPress();
}
