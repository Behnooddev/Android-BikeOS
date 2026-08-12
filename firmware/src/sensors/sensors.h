#pragma once
// Sensor module - Hall (wheel/cadence) + rear-distance sensor, both real.
// Motion/IMU (MPU6050) lives in motion.h/motion.cpp instead (kept separate
// since alarm.cpp needs both this module's wheel-pulse timing AND motion's
// accel data - splitting them avoids a circular dependency). Battery
// (INA219) lives in power.h/power.cpp.
//
// Deliberate architecture choice: this module reports RAW wheel/cadence
// RPM, not speed in km/h or distance in km. Converting RPM to speed needs
// wheel circumference, which is bike-profile data the phone owns (Room),
// not something the firmware should need synced to it. Speed/distance
// conversion happens on the Android side (see SensorRepository.kt) - same
// reasoning as calories staying phone-side (rider-specific data lives with
// the phone's data layer, not the firmware).
//
// Phase J: rear-distance backend (VL53L1X I2C vs HC-SR04 ultrasonic) and
// Hall sensor interface (digital DO vs analog AO, active-low vs
// active-high) are both selected at compile time via
// config/sensor_backend_config.h - the function signatures below stay
// identical regardless of which backend is active, so callers (ble_service,
// alarm.cpp) never need to know or care.

#include <cstdint>

namespace bikeos::sensors {
    void init();   // Wire.begin() (if needed) + rear-sensor init/config + Hall setup
    void poll();   // non-blocking: services the rear sensor and recomputes RPM windows

    /** Latest rear-object distance in millimeters. Returns 0 if the sensor
     *  failed to initialize or no reading has arrived yet - callers must
     *  treat 0 as "no data", not "object at 0mm". */
    uint16_t getRearDistanceMm();

    /** True once the rear sensor has finished initializing successfully. */
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
