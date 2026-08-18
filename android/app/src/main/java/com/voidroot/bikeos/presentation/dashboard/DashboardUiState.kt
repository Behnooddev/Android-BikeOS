package com.voidroot.bikeos.presentation.dashboard

import com.voidroot.bikeos.data.repository.SensorSourceType

/**
 * Everything the Dashboard screen needs to render one frame.
 * Sensor fields come from [com.voidroot.bikeos.data.repository.SensorRepository]
 * - real telemetry when connected, honest zeros + isConnected=false when
 * not (see that class's kdoc: the cluster never shows fake data).
 *
 * [sensorSource] (Phase K) tells widgets whether the current snapshot came
 * from the ESP32 or the phone - [cadenceRpm] and [batteryPercent] are
 * structurally unavailable when `sensorSource == SensorSourceType.PHONE`
 * (not "currently 0"), so those widgets must render a permanent dash in
 * that case rather than "0".
 *
 * [frontGear]/[rearGear] come from Room (via BikeRepository), and
 * [enabledWidgetKeys] drives which bottom-row cards the Dashboard actually
 * renders (per the Appearance screen's toggles).
 */
data class DashboardUiState(
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 45f,
    val distanceKm: Float = 0f,
    val calories: Int = 0,
    val cadenceRpm: Int = 0,
    val batteryPercent: Int = 0,
    val isConnected: Boolean = false,
    val sensorSource: SensorSourceType = SensorSourceType.NONE,
    val frontGear: Int = 1,
    val rearGear: Int = 1,
    val rideMode: RideMode = RideMode.CRUISE,
    val currentTime: String = "--:--",
    val enabledWidgetKeys: Set<String> = emptySet(),
    val isRideActive: Boolean = false,
    /** Empty when gear suggestions are off, not moving/pedaling, or on-target with nothing to say - see GearSuggestionEngine. */
    val gearSuggestionLabel: String = ""
)
