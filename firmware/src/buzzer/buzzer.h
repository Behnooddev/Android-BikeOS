#pragma once
// Shared piezo buzzer output - owns BUZZER_PIN (moved here from alarm.cpp,
// which used to drive the pin directly - see buzzer.cpp for the wiring note).
//
// Two independent use patterns share this one GPIO:
//   1. alarm.cpp's continuous triggered-alarm buzz, synced to its own blink
//      timer - uses drive(bool) for direct per-loop on/off control.
//   2. Short one-shot "N beeps" confirmation patterns (e.g. the keyless
//      "system on" ignition chime - see ble_service.cpp's
//      BIKEOS_CMD_SYSTEM_ON handler) - uses beepPattern(), serviced
//      non-blockingly from poll() so it never stalls BLE/sensor polling
//      the way a delay()-based beep sequence would (see CONTRIBUTING.md's
//      "non-blocking only" firmware convention).
//
// These two callers are not expected to run at the same time in practice
// (an armed+triggered alarm is a security event; the ignition chime is a
// separate, deliberate rider action) - alarm.cpp calls lockForExternalControl()
// while it's actively driving the pin itself, and beepPattern() simply no-ops
// while locked, so the two can never fight over the same GPIO.

#include <cstdint>

namespace bikeos::buzzer {
    void init();
    void poll(); // services any in-progress beepPattern() - non-blocking

    /** Direct on/off - used by alarm.cpp's own blink-synced buzz while triggered. */
    void drive(bool on);

    /** Alarm.cpp calls this true while it's mid-trigger (directly driving
     *  the pin via drive()) and false once it's done, so beepPattern()
     *  knows not to fight it for the GPIO. */
    void lockForExternalControl(bool locked);

    /** Starts a short non-blocking beep sequence (e.g. 3 confirmation
     *  beeps at ignition-on). No-ops if locked (see above), if one is
     *  already in progress, or if count is 0. */
    void beepPattern(uint8_t count, unsigned long onMs = 120, unsigned long offMs = 120);
}
