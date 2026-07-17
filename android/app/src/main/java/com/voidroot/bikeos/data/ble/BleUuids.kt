package com.voidroot.bikeos.data.ble

import java.util.UUID

/**
 * BLE GATT identifiers. MUST stay byte-for-byte identical to
 * `firmware/src/bluetooth/ble_uuids.h` - there is no shared code-gen
 * between the two projects yet (flagged as tech debt in the architecture
 * review's "core-ble-protocol" suggestion), so any change here has to be
 * mirrored by hand on the firmware side or the two sides simply won't find
 * each other's services.
 *
 * Per the architecture review's Conflict #2 resolution: Device ID and
 * Protocol Version are read ONCE from the Device Information Service right
 * after connecting - they are NOT repeated in every Sensor Data packet.
 */
object BleUuids {
    private const val BASE_SUFFIX = "b5a3-f393-e0a9-e50e24dcca9e"
    private fun uuid(prefix: String) = UUID.fromString("$prefix-$BASE_SUFFIX")

    val DEVICE_INFO_SERVICE: UUID = uuid("6e400001")
    val DEVICE_ID_CHARACTERISTIC: UUID = uuid("6e400002")
    val FIRMWARE_VERSION_CHARACTERISTIC: UUID = uuid("6e400003")
    val PROTOCOL_VERSION_CHARACTERISTIC: UUID = uuid("6e400004")

    val SENSOR_DATA_SERVICE: UUID = uuid("6e400010")
    val SENSOR_DATA_CHARACTERISTIC: UUID = uuid("6e400011")

    val CONTROL_SERVICE: UUID = uuid("6e400020")
    val CONTROL_COMMAND_CHARACTERISTIC: UUID = uuid("6e400021")

    /** Standard BLE Client Characteristic Configuration Descriptor - not BikeOS-specific. */
    val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
