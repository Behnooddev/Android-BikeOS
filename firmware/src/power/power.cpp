#include "power.h"
#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_INA219.h>

// Library: Adafruit INA219 Arduino library.
//   PlatformIO: lib_deps = adafruit/Adafruit INA219 @ ^1.2.3 (see platformio.ini)
//   Wiring: VIN -> 3.3V or 5V (INA219 breakouts are usually 3-5.5V tolerant -
//   check your specific board), GND -> GND, SDA -> GPIO 21, SCL -> GPIO 22
//   (same I2C bus as the VL53L1X in sensors.cpp - Wire.begin() is already
//   called there; calling it again here is harmless/idempotent).
//
// Battery percentage estimate: INA219 measures voltage/current, not charge
// directly. The voltage-based curve below is calibrated for a single-cell
// (3.7V nominal) Li-ion/LiPo pack - if a different pack chemistry/cell
// count is used, BATTERY_MIN_V/BATTERY_MAX_V below need updating, or this
// whole estimate should be swapped for a proper coulomb-counting approach
// later. This is a reasonable v1, not a precise fuel gauge.

#define BATTERY_MIN_V 3.3f  // ~0% for a single-cell Li-ion under light load
#define BATTERY_MAX_V 4.2f  // 100% - fully charged single-cell Li-ion

#define POLL_INTERVAL_MS 2000

namespace bikeos::power {
namespace {
    Adafruit_INA219 ina219;
    bool sensorReady = false;

    float lastBusVoltage = 0.0f;
    float lastCurrentMa = 0.0f;
    uint8_t lastBatteryPercent = 0;
    unsigned long lastPollMs = 0;

    uint8_t voltageToPercent(float voltage) {
        if (voltage <= BATTERY_MIN_V) return 0;
        if (voltage >= BATTERY_MAX_V) return 100;
        float fraction = (voltage - BATTERY_MIN_V) / (BATTERY_MAX_V - BATTERY_MIN_V);
        return (uint8_t)(fraction * 100.0f);
    }
}

void init() {
    Wire.begin(); // idempotent if sensors::init() already called it

    if (!ina219.begin()) {
        Serial.println("[Power] INA219 init FAILED - check wiring/address");
        sensorReady = false;
        return;
    }

    sensorReady = true;
    lastPollMs = millis();
    Serial.println("[Power] INA219 ready");
}

void poll() {
    if (!sensorReady) return;
    if (millis() - lastPollMs < POLL_INTERVAL_MS) return;
    lastPollMs = millis();

    lastBusVoltage = ina219.getBusVoltage_V();
    lastCurrentMa = ina219.getCurrent_mA();
    lastBatteryPercent = voltageToPercent(lastBusVoltage);
}

float getBusVoltage() { return lastBusVoltage; }
float getCurrentMa() { return lastCurrentMa; }
uint8_t getBatteryPercent() { return lastBatteryPercent; }
bool isPowerSensorReady() { return sensorReady; }

} // namespace bikeos::power
