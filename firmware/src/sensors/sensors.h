#pragma once
// Sensor module - Hall (wheel/cadence) + VL53L1X (rear distance), both
// real. Motion/IMU (MPU6050) lives in motion.h/motion.cpp instead (kept
// separate since alarm.cpp needs both this module's wheel-pulse timing AND
// motion's accel data - splitting them avoids a circular dependency).
// Battery (INA219) lives in power.h/power.cpp.
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

    /** millis() timestamp of the most recent wheel Hall pulse - used by
     *  alarm.cpp to detect "wheel moved while armed" without needing its
     *  own duplicate ISR. 0 if no pulse has ever been seen. */
    unsigned long getLastWheelPulseMs();
}
