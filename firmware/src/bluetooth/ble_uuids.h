#pragma once
// GATT UUIDs - MUST stay byte-for-byte identical to
// `android/.../data/ble/BleUuids.kt`. No shared code-gen between the two
// projects yet (flagged as tech debt in the architecture review); any
// change here has to be mirrored by hand on the Android side.

#define BIKEOS_DEVICE_INFO_SERVICE_UUID              "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_DEVICE_ID_CHARACTERISTIC_UUID          "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_FIRMWARE_VERSION_CHARACTERISTIC_UUID   "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_PROTOCOL_VERSION_CHARACTERISTIC_UUID   "6e400004-b5a3-f393-e0a9-e50e24dcca9e"

#define BIKEOS_SENSOR_DATA_SERVICE_UUID               "6e400010-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_SENSOR_DATA_CHARACTERISTIC_UUID         "6e400011-b5a3-f393-e0a9-e50e24dcca9e"

#define BIKEOS_CONTROL_SERVICE_UUID                   "6e400020-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_CONTROL_COMMAND_CHARACTERISTIC_UUID    "6e400021-b5a3-f393-e0a9-e50e24dcca9e"

// Packet message types - keep in sync with BlePacket.kt
#define BIKEOS_MSG_TYPE_SENSOR_DATA      0x01
#define BIKEOS_MSG_TYPE_DEVICE_STATUS    0x03
#define BIKEOS_MSG_TYPE_BUTTON_EVENT     0x04
#define BIKEOS_MSG_TYPE_CONTROL_COMMAND  0x10
#define BIKEOS_MSG_TYPE_ERROR            0xFF

// Button event IDs (payload byte for BIKEOS_MSG_TYPE_BUTTON_EVENT) - keep
// in sync with BlePacket.kt's DeviceButtonEvent.
#define BIKEOS_BUTTON_EVENT_MODE       0x01
#define BIKEOS_BUTTON_EVENT_GEAR_UP    0x02
#define BIKEOS_BUTTON_EVENT_GEAR_DOWN  0x03
