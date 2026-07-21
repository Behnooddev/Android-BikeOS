#include <Arduino.h>
#include "../config/device_config.h"
#include "../sensors/sensors.h"
#include "../bluetooth/ble_service.h"
#include "../controls/controls.h"
#include "../power/power.h"
#include "../motion/motion.h"
#include "../alarm/alarm.h"

// BikeOS Firmware entry point.
// Current state: every hardware module the builder has confirmed
// purchased is real -
//   sensors    - Hall wheel/cadence RPM (ISR), VL53L1X rear distance
//   ble        - advertising + all 3 GATT services, real sensor/button/
//                alarm events out, light + alarm arm/disarm commands in
//   controls   - front/rear/body light MOSFETs, 4 handlebar buttons
//   power      - INA219 battery voltage/current/percent
//   motion     - MPU6050 accel magnitude (feeds the alarm system)
//   alarm      - anti-theft: wheel-pulse-while-armed OR motion-delta
//                trigger, buzzer + blinking lights, BLE alarm events
//
// init() order matters a little: sensors::init() calls Wire.begin()
// first, and power::init()/motion::init() both rely on that same I2C bus
// already being up (their own Wire.begin() calls are idempotent no-ops if
// so, defensive if not).

void setup() {
    Serial.begin(115200);
    Serial.println(BIKEOS_DEVICE_NAME);
    Serial.println(BIKEOS_FIRMWARE_VERSION);

    bikeos::sensors::init();
    bikeos::power::init();
    bikeos::motion::init();
    bikeos::controls::init();
    bikeos::alarm::init();
    bikeos::ble::init();
}

void loop() {
    bikeos::sensors::poll();
    bikeos::power::poll();
    bikeos::motion::poll();
    bikeos::controls::poll();
    bikeos::alarm::poll();
    bikeos::ble::tick();
}
