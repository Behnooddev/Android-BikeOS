package com.voidroot.bikeos.data.repository

/**
 * Which physical source a [SensorSnapshot] came from. Phase K: BikeOS can
 * run with the ESP32 dashboard hardware, or "hardware-free" using only the
 * phone's own GPS/accelerometer (see [PhoneSensorSource]).
 *
 * This exists so the UI can distinguish "not connected" from "connected,
 * but to a source that structurally cannot produce this field" - e.g.
 * cadence and battery are physically impossible to derive from a phone
 * (no pedal sensor, and "phone battery" isn't "bike battery"). Those
 * fields must render as a permanent dash in [PHONE] mode, not as 0 (which
 * would look identical to "disconnected"), and not as a fake/estimated
 * value (violates the no-fake-data rule the ESP32 path already follows).
 */
enum class SensorSourceType {
    /** No source active - the disconnected clock-only stream. */
    NONE,

    /** Real telemetry from the ESP32 over BLE (Phase H protocol). */
    ESP32,

    /** GPS speed/distance + phone accelerometer. No cadence, no battery. */
    PHONE
}
