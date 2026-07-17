package com.voidroot.bikeos.data.ble

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Raw values a Sensor Data notification carries.
 *
 * Speed/distance are NOT here - the firmware reports raw wheel rotation
 * rate, not speed, because converting that to km/h needs wheel
 * circumference (bike-profile data Android owns, not the firmware). See
 * [com.voidroot.bikeos.data.repository.SensorRepository] for the
 * conversion. Calories are absent for the same reasoning (rider-weight
 * dependent, phone-side only).
 *
 * [batteryPercent] is 0 when the firmware's INA219 isn't ready yet -
 * callers should treat 0 as "unknown", not "empty".
 */
data class SensorPayload(
    val wheelRpm: Int,
    val cadenceRpm: Int,
    val batteryPercent: Int
)

/** A handlebar button press forwarded from the ESP32 - see controls.h on the firmware side for why these are events, not state. */
enum class DeviceButtonEvent(val id: Int) {
    MODE(0x01), GEAR_UP(0x02), GEAR_DOWN(0x03);

    companion object {
        fun fromId(id: Int): DeviceButtonEvent? = entries.find { it.id == id }
    }
}

/**
 * Commands Android can send to the ESP32 over the Control Service.
 * IDs must stay in sync with `firmware/src/bluetooth/ble_service.cpp`'s
 * command switch.
 */
enum class ControlCommand(val id: Int) {
    FRONT_LIGHT_ON(0x01), FRONT_LIGHT_OFF(0x02),
    REAR_LIGHT_ON(0x03), REAR_LIGHT_OFF(0x04),
    BODY_LIGHT_ON(0x05), BODY_LIGHT_OFF(0x06),
    SET_MODE_ECO(0x10), SET_MODE_CRUISE(0x11), SET_MODE_SPRINT(0x12),
    SET_MODE_CLIMB(0x13), SET_MODE_DOWNHILL(0x14),
    UPDATE_FRONT_GEAR(0x20), UPDATE_REAR_GEAR(0x21),
    REQUEST_STATUS(0x30), RESET_DEVICE(0x31), SYNC_TIME(0x32)
}

/**
 * Packet format (per the BLE architecture doc):
 *
 * ```
 * [messageType:1][timestampEpochSec:4 LE][payloadLength:1][payload:N][checksum:1]
 * ```
 *
 * Device ID / Protocol Version are NOT in this header - see [BleUuids] kdoc.
 * Checksum is a simple running XOR over every byte before it - adequate for
 * catching truncated/garbled notifications on a short-range link; NOT a
 * cryptographic integrity check (real authentication is a later hardening
 * pass - see the architecture review's Security Considerations).
 *
 * Sensor Data and Button Event packets both arrive on the same notify
 * characteristic (the firmware doesn't have a separate characteristic for
 * button events) - [decode] peeks the messageType byte and returns the
 * right sealed variant so callers don't need to know that detail.
 */
object BlePacket {
    const val TYPE_SENSOR_DATA = 0x01
    const val TYPE_DEVICE_STATUS = 0x03
    const val TYPE_BUTTON_EVENT = 0x04
    const val TYPE_CONTROL_COMMAND = 0x10
    const val TYPE_ERROR = 0xFF

    private const val HEADER_SIZE = 6 // messageType(1) + timestamp(4) + payloadLength(1)
    private const val SENSOR_PAYLOAD_SIZE = 5 // wheelRpm(2) + cadenceRpm(2) + battery(1)
    private const val BUTTON_EVENT_PAYLOAD_SIZE = 1

    sealed class DecodedNotification {
        data class SensorData(val payload: SensorPayload) : DecodedNotification()
        data class ButtonEvent(val event: DeviceButtonEvent) : DecodedNotification()
    }

    private fun xorChecksum(bytes: ByteArray, length: Int): Byte {
        var c = 0
        for (i in 0 until length) c = c xor bytes[i].toInt()
        return c.toByte()
    }

    private fun verifyChecksum(bytes: ByteArray): Boolean =
        bytes.isNotEmpty() && bytes.last() == xorChecksum(bytes, bytes.size - 1)

    /** Decodes whichever packet type arrived on the Sensor Data characteristic. Returns null for anything malformed/unrecognized. */
    fun decode(bytes: ByteArray): DecodedNotification? {
        if (bytes.size < HEADER_SIZE + 1) return null
        if (!verifyChecksum(bytes)) return null // corrupt/truncated - drop it

        val messageType = bytes[0].toInt() and 0xFF
        val payloadLength = bytes[5].toInt() and 0xFF
        if (bytes.size != HEADER_SIZE + payloadLength + 1) return null

        return when (messageType) {
            TYPE_SENSOR_DATA -> {
                if (payloadLength != SENSOR_PAYLOAD_SIZE) return null
                val buffer = ByteBuffer.wrap(bytes, HEADER_SIZE, SENSOR_PAYLOAD_SIZE).order(ByteOrder.LITTLE_ENDIAN)
                val wheelRpm = buffer.short.toInt() and 0xFFFF
                val cadenceRpm = buffer.short.toInt() and 0xFFFF
                val batteryPercent = buffer.get().toInt() and 0xFF
                DecodedNotification.SensorData(SensorPayload(wheelRpm, cadenceRpm, batteryPercent))
            }
            TYPE_BUTTON_EVENT -> {
                if (payloadLength != BUTTON_EVENT_PAYLOAD_SIZE) return null
                val id = bytes[HEADER_SIZE].toInt() and 0xFF
                DeviceButtonEvent.fromId(id)?.let { DecodedNotification.ButtonEvent(it) }
            }
            else -> null
        }
    }

    /** Builds a Control Command packet. [param] is a single byte (e.g. a gear number); omit for parameterless commands. */
    fun encodeControlCommand(command: ControlCommand, param: Int? = null): ByteArray {
        val payload = if (param != null) {
            byteArrayOf(command.id.toByte(), (param and 0xFF).toByte())
        } else {
            byteArrayOf(command.id.toByte())
        }

        val packet = ByteBuffer.allocate(HEADER_SIZE + payload.size + 1).order(ByteOrder.LITTLE_ENDIAN)
        packet.put(TYPE_CONTROL_COMMAND.toByte())
        packet.putInt((System.currentTimeMillis() / 1000).toInt())
        packet.put(payload.size.toByte())
        packet.put(payload)

        val bytesSoFar = packet.array().copyOf(HEADER_SIZE + payload.size)
        val checksum = xorChecksum(bytesSoFar, bytesSoFar.size)
        packet.put(checksum)

        return packet.array()
    }
}
