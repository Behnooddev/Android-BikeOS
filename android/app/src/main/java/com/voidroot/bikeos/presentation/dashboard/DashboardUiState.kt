package com.voidroot.bikeos.presentation.dashboard

/**
 * Everything the Dashboard screen needs to render one frame.
 * Sensor fields come from [com.voidroot.bikeos.data.repository.SensorRepository]
 * (real BLE data when connected, [com.voidroot.bikeos.data.fake.FakeSensorDataSource]
 * otherwise) - the shape is deliberately kept BLE-plausible.
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
    val batteryPercent: Int = 100,
    val isConnected: Boolean = false,
    val frontGear: Int = 1,
    val rearGear: Int = 1,
    val rideMode: RideMode = RideMode.CRUISE,
    val currentTime: String = "--:--",
    val enabledWidgetKeys: Set<String> = emptySet(),
    val isRideActive: Boolean = false
)
