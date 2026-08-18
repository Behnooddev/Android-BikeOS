package com.voidroot.bikeos.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * What the Dashboard actually renders. Per the hard product rule "the
 * cluster must never show fake data": every field here is either real
 * telemetry from an active [source] or a real system value (the clock) -
 * there is no simulated generator. While disconnected, every sensor field
 * is exactly 0 and [isConnected] is false; the UI is responsible for
 * rendering that honestly (dashed/greyed out), not for hiding the zeros.
 *
 * Phase K: [source] tells the UI *which* fields are honestly absent vs.
 * just currently zero. [SensorSourceType.PHONE] never populates
 * [cadenceRpm] or [batteryPercent] - not "currently 0", but structurally
 * unavailable - so the UI must render those two as a permanent dash
 * whenever `source == PHONE`, the same way it dashes everything when
 * `isConnected == false`.
 */
data class SensorSnapshot(
    val speedKmh: Float = 0f,
    val distanceKm: Float = 0f,
    val calories: Int = 0,
    val cadenceRpm: Int = 0,
    val batteryPercent: Int = 0,
    val isConnected: Boolean = false,
    val currentTime: String = "--:--",
    /** MPU6050 (ESP32) or phone accelerometer magnitude in g (~1.0g at rest). 0f while disconnected. Protocol 1.2 / Phase H, extended to phone in Phase K. */
    val accelG: Float = 0f,
    val source: SensorSourceType = SensorSourceType.NONE
)

/**
 * Orchestrates between [BleSensorSource] (ESP32 hardware) and
 * [PhoneSensorSource] (Phase K hardware-free mode), switching live off
 * [AppSettings.hardwareFreeModeEnabled] - per product decision, switching
 * mid-ride is allowed, so this re-subscribes immediately on toggle rather
 * than requiring a restart.
 */
class SensorRepository @Inject constructor(
    private val bleSensorSource: BleSensorSource,
    private val phoneSensorSource: PhoneSensorSource,
    private val settingsRepository: SettingsRepository
) {
    fun stream(): Flow<SensorSnapshot> = settingsRepository.observe().flatMapLatest { settings ->
        if (settings.hardwareFreeModeEnabled) {
            phoneSensorSource.stream()
        } else {
            bleSensorSource.stream()
        }
    }
}
