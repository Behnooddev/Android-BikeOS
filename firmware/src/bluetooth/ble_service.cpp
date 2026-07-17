#include "ble_service.h"
#include "ble_uuids.h"
#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <string.h>
#include "../config/device_config.h"
#include "../sensors/sensors.h"
#include "../controls/controls.h"
#include "../power/power.h"

namespace bikeos::ble {
namespace {
    BLEServer* server = nullptr;
    BLECharacteristic* sensorDataCharacteristic = nullptr; // also used to notify button events
    BLECharacteristic* controlCommandCharacteristic = nullptr;
    bool deviceConnected = false;

    unsigned long lastNotifyMs = 0;
    const unsigned long NOTIFY_INTERVAL_MS = 500;

    uint8_t xorChecksum(const uint8_t* bytes, size_t length) {
        uint8_t c = 0;
        for (size_t i = 0; i < length; i++) c ^= bytes[i];
        return c;
    }

    /**
     * Builds and sends one Sensor Data packet from REAL sensor readings:
     * [type:1][timestampSec:4 LE][payloadLen:1][wheelRpm:2][cadenceRpm:2][battery:1][checksum:1]
     *
     * Speed/distance are NOT sent - the firmware doesn't know wheel
     * circumference (that's bike-profile data Android owns). Android
     * converts wheelRpm to speed/distance itself. See sensors.h kdoc and
     * SensorRepository.kt for the full reasoning.
     *
     * Layout MUST match BlePacket.decode()'s SENSOR_DATA branch on the Android side.
     */
    void sendSensorData() {
        if (!deviceConnected || sensorDataCharacteristic == nullptr) return;

        const uint8_t payloadLength = 5; // wheelRpm(2) + cadenceRpm(2) + battery(1)
        uint8_t packet[6 + 5 + 1];

        packet[0] = BIKEOS_MSG_TYPE_SENSOR_DATA;
        uint32_t timestampSec = (uint32_t)(millis() / 1000);
        memcpy(&packet[1], &timestampSec, sizeof(timestampSec));
        packet[5] = payloadLength;

        uint16_t wheelRpm = bikeos::sensors::getWheelRpm();
        uint16_t cadenceRpm = bikeos::sensors::getCadenceRpm();
        // Battery: real INA219 reading when available, otherwise report
        // 0 rather than a fabricated number - the app should treat 0 as
        // "unknown" until the power sensor is confirmed ready, not "empty".
        uint8_t battery = bikeos::power::isPowerSensorReady()
            ? bikeos::power::getBatteryPercent()
            : 0;

        memcpy(&packet[6], &wheelRpm, sizeof(wheelRpm));
        memcpy(&packet[8], &cadenceRpm, sizeof(cadenceRpm));
        packet[10] = battery;

        packet[11] = xorChecksum(packet, 11);

        sensorDataCharacteristic->setValue(packet, sizeof(packet));
        sensorDataCharacteristic->notify();
    }

    /**
     * Sends a one-byte Button Event over the same characteristic as Sensor
     * Data (Android's decoder branches on the messageType byte). Mode/Gear
     * button presses are events, not state - Android owns what the actual
     * next mode/gear value is (see controls.h kdoc).
     */
    void sendButtonEvent(uint8_t buttonId) {
        if (!deviceConnected || sensorDataCharacteristic == nullptr) return;

        uint8_t packet[6 + 1 + 1];
        packet[0] = BIKEOS_MSG_TYPE_BUTTON_EVENT;
        uint32_t timestampSec = (uint32_t)(millis() / 1000);
        memcpy(&packet[1], &timestampSec, sizeof(timestampSec));
        packet[5] = 1; // payload length
        packet[6] = buttonId;
        packet[7] = xorChecksum(packet, 7);

        sensorDataCharacteristic->setValue(packet, sizeof(packet));
        sensorDataCharacteristic->notify();
    }

    void pollButtonEvents() {
        if (!deviceConnected) return;
        if (bikeos::controls::consumeModeButtonPress()) sendButtonEvent(BIKEOS_BUTTON_EVENT_MODE);
        if (bikeos::controls::consumeGearUpButtonPress()) sendButtonEvent(BIKEOS_BUTTON_EVENT_GEAR_UP);
        if (bikeos::controls::consumeGearDownButtonPress()) sendButtonEvent(BIKEOS_BUTTON_EVENT_GEAR_DOWN);
    }

    class ServerCallbacks : public BLEServerCallbacks {
        void onConnect(BLEServer* s) override {
            deviceConnected = true;
            Serial.println("[BLE] Client connected");
        }
        void onDisconnect(BLEServer* s) override {
            deviceConnected = false;
            Serial.println("[BLE] Client disconnected - restarting advertising");
            BLEDevice::startAdvertising();
        }
    };

    /**
     * Validates incoming Control Service writes and now actually drives
     * hardware for light commands. Mode/gear "update" commands are parsed
     * and logged but don't change local state - Android is authoritative
     * for both (see controls.h kdoc); RESET_DEVICE/SYNC_TIME are parsed
     * but not yet acted on (no persistent RTC/config to reset/sync here).
     */
    class ControlCommandCallbacks : public BLECharacteristicCallbacks {
        void onWrite(BLECharacteristic* characteristic) override {
            std::string value = characteristic->getValue();
            if (value.length() < 7) {
                Serial.println("[BLE] Control command too short, dropped");
                return;
            }

            const uint8_t* bytes = reinterpret_cast<const uint8_t*>(value.data());
            uint8_t messageType = bytes[0];
            uint8_t payloadLength = bytes[5];

            if (value.length() != (size_t)(6 + payloadLength + 1)) {
                Serial.println("[BLE] Control command length mismatch, dropped");
                return;
            }

            uint8_t expectedChecksum = xorChecksum(bytes, value.length() - 1);
            if (bytes[value.length() - 1] != expectedChecksum) {
                Serial.println("[BLE] Control command checksum mismatch, dropped");
                return;
            }

            if (messageType != BIKEOS_MSG_TYPE_CONTROL_COMMAND) {
                Serial.println("[BLE] Unexpected message type on Control characteristic, dropped");
                return;
            }

            uint8_t commandId = bytes[6];

            switch (commandId) {
                case 0x01: bikeos::controls::setFrontLight(true);  break; // FRONT_LIGHT_ON
                case 0x02: bikeos::controls::setFrontLight(false); break; // FRONT_LIGHT_OFF
                case 0x03: bikeos::controls::setRearLight(true);   break; // REAR_LIGHT_ON
                case 0x04: bikeos::controls::setRearLight(false);  break; // REAR_LIGHT_OFF
                case 0x05: bikeos::controls::setBodyLight(true);   break; // BODY_LIGHT_ON
                case 0x06: bikeos::controls::setBodyLight(false);  break; // BODY_LIGHT_OFF
                default:
                    Serial.printf("[BLE] Control command 0x%02X received (no physical effect)\n", commandId);
                    break;
            }
        }
    };
}

void init() {
    BLEDevice::init(BIKEOS_DEVICE_NAME);
    server = BLEDevice::createServer();
    server->setCallbacks(new ServerCallbacks());

    // Device Information Service - read once at connect time.
    BLEService* deviceInfoService = server->createService(BIKEOS_DEVICE_INFO_SERVICE_UUID);

    auto* deviceIdChar = deviceInfoService->createCharacteristic(
        BIKEOS_DEVICE_ID_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ);
    deviceIdChar->setValue(BIKEOS_DEVICE_NAME);

    auto* firmwareVersionChar = deviceInfoService->createCharacteristic(
        BIKEOS_FIRMWARE_VERSION_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ);
    firmwareVersionChar->setValue(BIKEOS_FIRMWARE_VERSION);

    auto* protocolVersionChar = deviceInfoService->createCharacteristic(
        BIKEOS_PROTOCOL_VERSION_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ);
    protocolVersionChar->setValue(BIKEOS_PROTOCOL_VERSION);

    deviceInfoService->start();

    // Sensor Data Service - notify (Sensor Data + Button Event packets both
    // go out over this one characteristic; see sendButtonEvent() above).
    BLEService* sensorService = server->createService(BIKEOS_SENSOR_DATA_SERVICE_UUID);
    sensorDataCharacteristic = sensorService->createCharacteristic(
        BIKEOS_SENSOR_DATA_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
    sensorDataCharacteristic->addDescriptor(new BLE2902());
    sensorService->start();

    // Control Service - write.
    BLEService* controlService = server->createService(BIKEOS_CONTROL_SERVICE_UUID);
    controlCommandCharacteristic = controlService->createCharacteristic(
        BIKEOS_CONTROL_COMMAND_CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_WRITE);
    controlCommandCharacteristic->setCallbacks(new ControlCommandCallbacks());
    controlService->start();

    BLEAdvertising* advertising = BLEDevice::getAdvertising();
    advertising->addServiceUUID(BIKEOS_DEVICE_INFO_SERVICE_UUID);
    advertising->setScanResponse(true);
    advertising->setMinPreferred(0x06);
    advertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();

    Serial.println("[BLE] Advertising started as: " BIKEOS_DEVICE_NAME);
}

void tick() {
    pollButtonEvents();

    if (deviceConnected && millis() - lastNotifyMs >= NOTIFY_INTERVAL_MS) {
        sendSensorData();
        lastNotifyMs = millis();
    }
}

} // namespace bikeos::ble
