#pragma once
// ============================================================================
// BikeOS BLE Application Protocol - SINGLE SOURCE OF TRUTH (firmware side).
//
// Every message type, event ID, and control command ID used ANYWHERE in
// the firmware MUST be defined here, exactly once, and MUST match
// android/app/.../data/ble/BlePacket.kt byte-for-byte (that file is the
// Android-side single source of truth - the two must always be edited
// together).
//
// This file exists because of a real bug: BIKEOS_MSG_TYPE_ALARM_EVENT was
// referenced in ble_service.cpp without ever being defined anywhere,
// because message-type/command constants were scattered (some in
// ble_uuids.h, some as bare magic numbers in a switch statement). Do not
// add a new message type, event ID, or command ID anywhere else -
// add it here, and update BlePacket.kt in the same change.
//
// GATT service/characteristic UUIDs are a separate, lower-layer concern
// and stay in ble_uuids.h - this file is only the application-layer
// packet protocol (message types, payload shapes, event/command IDs).
// ============================================================================

// ---- Packet header (every packet, both directions) ----
// [messageType:1][timestampEpochSec:4 LE][payloadLength:1][payload:N][checksum:1 XOR-of-everything-before-it]

// ---- Message types (packet[0]) ----
#define BIKEOS_MSG_TYPE_SENSOR_DATA      0x01
#define BIKEOS_MSG_TYPE_DEVICE_STATUS    0x03
#define BIKEOS_MSG_TYPE_BUTTON_EVENT     0x04
#define BIKEOS_MSG_TYPE_ALARM_EVENT      0x05
#define BIKEOS_MSG_TYPE_CONTROL_COMMAND  0x10
#define BIKEOS_MSG_TYPE_ERROR            0xFF

// ---- Sensor Data payload (7 bytes) ----
// wheelRpm(u16 LE) + cadenceRpm(u16 LE) + batteryPercent(u8) + accelMilliG(u16 LE)
// Speed/distance are NOT sent - see sensors.h kdoc (wheel circumference is
// bike-profile data Android owns, firmware only reports raw RPM).
// accelMilliG: Phase H addition (protocol 1.1 -> 1.2) - magnitude of the
// MPU6050 acceleration vector in milli-g (1000 = 1.0g, i.e. at rest under
// gravity), from motion::getAccelMagnitude() * 1000, clamped to fit u16.
// Feeds Android's real riding-style analysis (see HomeViewModel's
// ridingStyleFrom) - previously only used internally for the anti-theft
// alarm and never left the firmware at all.
#define BIKEOS_SENSOR_PAYLOAD_SIZE 7

// ---- Button Event payload (1 byte: which button) ----
// IDs must match BlePacket.kt's DeviceButtonEvent enum exactly.
#define BIKEOS_BUTTON_EVENT_PAYLOAD_SIZE 1
#define BIKEOS_BUTTON_EVENT_MODE       0x01
#define BIKEOS_BUTTON_EVENT_GEAR_UP    0x02
#define BIKEOS_BUTTON_EVENT_GEAR_DOWN  0x03

// ---- Alarm Event payload (1 byte: triggered/cleared) ----
#define BIKEOS_ALARM_EVENT_PAYLOAD_SIZE 1
#define BIKEOS_ALARM_EVENT_CLEARED    0x00
#define BIKEOS_ALARM_EVENT_TRIGGERED  0x01

// ---- Control Commands (payload[0] when messageType == CONTROL_COMMAND) ----
// IDs must match BlePacket.kt's ControlCommand enum exactly.
#define BIKEOS_CMD_FRONT_LIGHT_ON     0x01
#define BIKEOS_CMD_FRONT_LIGHT_OFF    0x02
#define BIKEOS_CMD_REAR_LIGHT_ON      0x03
#define BIKEOS_CMD_REAR_LIGHT_OFF     0x04
#define BIKEOS_CMD_BODY_LIGHT_ON      0x05
#define BIKEOS_CMD_BODY_LIGHT_OFF     0x06
#define BIKEOS_CMD_SET_MODE_ECO       0x10
#define BIKEOS_CMD_SET_MODE_CRUISE    0x11
#define BIKEOS_CMD_SET_MODE_SPRINT    0x12
#define BIKEOS_CMD_SET_MODE_CLIMB     0x13
#define BIKEOS_CMD_SET_MODE_DOWNHILL  0x14
#define BIKEOS_CMD_UPDATE_FRONT_GEAR  0x20
#define BIKEOS_CMD_UPDATE_REAR_GEAR   0x21
#define BIKEOS_CMD_REQUEST_STATUS     0x30
#define BIKEOS_CMD_RESET_DEVICE       0x31
#define BIKEOS_CMD_SYNC_TIME          0x32
#define BIKEOS_CMD_ARM_ALARM          0x40
#define BIKEOS_CMD_DISARM_ALARM       0x41
