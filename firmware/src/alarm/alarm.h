#pragma once
// Anti-theft alarm - orchestrates sensors (wheel Hall pulses + MPU6050
// motion), controls (buzzer + blinking lights), and BLE (notifying Android
// the instant it triggers, for the quick-disarm password dialog).
//
// Two independent trigger conditions, either one fires it:
//   1. Wheel Hall pulse detected while armed (bike physically rolled).
//   2. MPU6050 accel magnitude changes sharply across a ~7s window (bike
//      picked up, dropped, shaken, etc. without necessarily rolling).
//
// Disarm is Android-authoritative: the phone verifies the account password
// locally, then sends DISARM_ALARM over the Control Service (see
// ble_service.cpp) - this module never checks a password itself, it just
// reacts to arm()/disarm() being called.

#include <cstdint>

namespace bikeos::alarm {
    void init();   // sets up the buzzer GPIO
    void poll();   // checks triggers when armed, drives buzzer/blink when triggered

    void arm();
    void disarm(); // also clears a triggered state

    bool isArmed();
    bool isTriggered();
}
