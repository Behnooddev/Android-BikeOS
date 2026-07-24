#pragma once
// GATT service/characteristic UUIDs - MUST stay byte-for-byte identical to
// `android/.../data/ble/BleUuids.kt`. No shared code-gen between the two
// projects yet (flagged as tech debt in the architecture review); any
// change here has to be mirrored by hand on the Android side.
//
// Application-layer protocol constants (message types, event/command IDs,
// payload sizes) live in `../protocol/bikeos_protocol.h`, NOT here - this
// file is GATT addressing only. See that file's kdoc for why they're
// deliberately kept separate and centralized.

#define BIKEOS_DEVICE_INFO_SERVICE_UUID              "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_DEVICE_ID_CHARACTERISTIC_UUID          "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_FIRMWARE_VERSION_CHARACTERISTIC_UUID   "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_PROTOCOL_VERSION_CHARACTERISTIC_UUID   "6e400004-b5a3-f393-e0a9-e50e24dcca9e"

#define BIKEOS_SENSOR_DATA_SERVICE_UUID               "6e400010-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_SENSOR_DATA_CHARACTERISTIC_UUID         "6e400011-b5a3-f393-e0a9-e50e24dcca9e"

#define BIKEOS_CONTROL_SERVICE_UUID                   "6e400020-b5a3-f393-e0a9-e50e24dcca9e"
#define BIKEOS_CONTROL_COMMAND_CHARACTERISTIC_UUID    "6e400021-b5a3-f393-e0a9-e50e24dcca9e"
