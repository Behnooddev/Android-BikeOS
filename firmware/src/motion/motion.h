#pragma once
// Motion/IMU module - MPU6050 (confirmed purchased). Used by alarm.cpp for
// theft-detection (sudden movement while armed) - slope estimation and
// crash detection (mentioned in the original hardware spec) are NOT
// implemented yet, this module only exposes what the alarm system needs
// today. Extend getX/Y/Z or add a dedicated slope function later without
// needing to change the alarm code.
//
// Phase J: the reading path (Adafruit library's calibrated output vs raw
// register reads + rolling average) is selected at compile time via
// config/sensor_backend_config.h - getAccelMagnitude()/isReady() behave
// identically either way, so alarm.cpp never needs to know which is active.

#include <cstdint>

namespace bikeos::motion {
    void init();   // Wire.begin() (idempotent - sensors.cpp already calls it) + MPU6050 init
    void poll();   // non-blocking: samples accel at a fixed interval, see motion.cpp

    /** Magnitude of the acceleration vector in g (~1.0g at rest, due to gravity). */
    float getAccelMagnitude();

    bool isReady();
}
