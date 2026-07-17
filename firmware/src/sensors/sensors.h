#pragma once
// Sensor module - Phase 4 (Hardware Integration), built incrementally as
// parts get confirmed by the builder:
//
//  - Wheel + cadence speed sensing: MH Sensor Series digital hall modules
//    (confirmed bought) - implemented for real below, ISR + rolling-window
//    RPM calculation.
//  - Rear distance: VL53L1X (confirmed bought, arriving soon) - implemented
//    for real in sensors.cpp.
//  - Motion/IMU (BMI270/MPU6050) and battery monitoring (INA219): NOT
//    implemented - hardware not yet confirmed. Add them here the same way
//    once purchased; battery is still simulated in ble_service.cpp for now.
//
// Deliberate architecture choice: this module reports RAW wheel/cadence
// RPM, not speed in km/h or distance in km. Converting RPM to speed needs
// wheel circumference, which is bike-profile data the phone owns (Room),
// not something the firmware should need synced to it. Speed/distance
// conversion happens on the Android side (see SensorRepository.kt) - same
// reasoning as calories staying phone-side (rider-specific data lives with
// the phone's data layer, not the firmware).

#include <cstdint>

namespace bikeos::sensors {
    void init();   // Wire.begin() + VL53L1X init/config + Hall ISR attachment
    void poll();   // non-blocking: services the VL53L1X and recomputes RPM windows

    /** Latest rear-object distance in millimeters. Returns 0 if the sensor
     *  failed to initialize or no reading has arrived yet - callers must
     *  treat 0 as "no data", not "object at 0mm". */
    uint16_t getRearDistanceMm();

    /** True once VL53L1X::init() has succeeded. */
    bool isRearSensorReady();

    /** Wheel rotations per minute, averaged over the last ~1s window.
     *  Assumes MAGNETS_PER_WHEEL magnet(s) per rotation - see sensors.cpp. */
    uint16_t getWheelRpm();

    /** Crank rotations per minute, averaged over the last ~1s window. */
    uint16_t getCadenceRpm();
}
