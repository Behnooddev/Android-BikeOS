package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.data.fake.FakeSensorDataSource
import com.voidroot.bikeos.data.fake.SensorSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.PI

/**
 * What the Dashboard actually consumes: one [SensorSnapshot] stream that
 * transparently switches between real BLE data (while connected) and the
 * Phase 1 fake generator (while not) - per the Communication Rules spec:
 * "When connection is lost: Android should... keep previous data safely"
 * and, just as importantly, never leave the cockpit UI showing nothing.
 *
 * Speed/distance conversion lives HERE, not in the firmware: the ESP32
 * reports raw wheel RPM (see [com.voidroot.bikeos.data.ble.SensorPayload]
 * kdoc), and this class turns that into km/h and accumulated km using the
 * wheel size from [BikeRepository] - the same "rider/bike-profile data
 * stays phone-side" reasoning calories already follow.
 */
class SensorRepository @Inject constructor(
    private val bleRepository: BleRepository,
    private val bikeRepository: BikeRepository
) {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun stream(): Flow<SensorSnapshot> = bleRepository.connectionState.flatMapLatest { state ->
        if (state is BleConnectionState.Connected) {
            realBleStream()
        } else {
            FakeSensorDataSource.stream()
        }
    }

    private fun realBleStream(): Flow<SensorSnapshot> {
        // Scoped to this flow's collection: reset every time the BLE
        // connection (re)becomes Connected, since flatMapLatest cancels
        // and recreates this flow on every upstream connectionState change.
        var cumulativeDistanceKm = 0f
        var lastEmitEpochMs = 0L

        return combine(bleRepository.sensorData, bikeRepository.observe()) { payload, bike ->
            val wheelCircumferenceMeters = (bike.wheelSizeInches * 0.0254f) * PI.toFloat()
            val speedKmh = payload.wheelRpm * wheelCircumferenceMeters * 60f / 1000f

            val now = System.currentTimeMillis()
            if (lastEmitEpochMs != 0L) {
                val hoursElapsed = (now - lastEmitEpochMs) / 3_600_000f
                cumulativeDistanceKm += speedKmh * hoursElapsed
            }
            lastEmitEpochMs = now

            SensorSnapshot(
                speedKmh = speedKmh,
                distanceKm = cumulativeDistanceKm,
                // Calories are computed on the Android side (rider-weight
                // dependent - see SensorPayload's kdoc), not by the
                // firmware. Real per-rider calorie calculation is a
                // follow-up (needs a MET-based formula against UserRepository's
                // weight); kept at 0 while connected rather than showing a
                // fake number from a real device.
                calories = 0,
                cadenceRpm = payload.cadenceRpm,
                batteryPercent = payload.batteryPercent,
                isConnected = true,
                currentTime = timeFormat.format(Date())
            )
        }
    }
}
