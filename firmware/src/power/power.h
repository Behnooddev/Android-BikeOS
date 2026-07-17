#pragma once
// INA219 battery monitoring - Phase 4, real hardware.
//
// Power-state management (Active/Idle/Deep-Sleep, flagged as a risk in the
// architecture review) is NOT implemented here yet - this module only
// reads and exposes battery telemetry. Sleep-state logic needs the Hall
// sensors (Phase 4, done) and BMI270/MPU6050 motion detection (not yet
// purchased) working together, so it's deferred until that sensor exists.

#include <cstdint>

namespace bikeos::power {
    void init(); // INA219 I2C init (shares the Wire bus set up in sensors.cpp)
    void poll(); // periodic voltage/current/percentage read

    float getBusVoltage();      // volts
    float getCurrentMa();       // milliamps, positive = discharging
    uint8_t getBatteryPercent(); // 0-100, estimated from voltage (see power.cpp)
    bool isPowerSensorReady();
}
