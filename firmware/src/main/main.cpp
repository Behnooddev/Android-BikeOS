#include <Arduino.h>
#include "../config/device_config.h"
#include "../sensors/sensors.h"
#include "../bluetooth/ble_service.h"
#include "../controls/controls.h"
#include "../power/power.h"

// BikeOS Firmware entry point.
// Current state (Phase 4): all four hardware modules are real -
//   sensors    - Hall wheel/cadence RPM (ISR), VL53L1X rear distance
//   ble        - advertising + all 3 GATT services, real sensor data +
//                button events out, light commands in
//   controls   - front/rear/body light MOSFETs, 4 handlebar buttons
//   power      - INA219 battery voltage/current/percent
// Motion/IMU (BMI270/MPU6050) is the only originally-planned sensor still
// not wired in - not purchased yet. See docs/06_PHASE_PLAN.md and the
// phase summary docs for history.

void setup() {
    Serial.begin(115200);
    Serial.println(BIKEOS_DEVICE_NAME);
    Serial.println(BIKEOS_FIRMWARE_VERSION);

    bikeos::sensors::init();
    bikeos::ble::init();
    bikeos::controls::init();
    bikeos::power::init();
}

void loop() {
    bikeos::sensors::poll();
    bikeos::ble::tick();
    bikeos::controls::poll();
    bikeos::power::poll();
}
