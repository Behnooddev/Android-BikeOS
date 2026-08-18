package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.ble.BleConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.PI

/**
 * ESP32-over-BLE sensor source. This is the original (Phase H) data path,
 * unchanged in behavior - just moved out of [SensorRepository] so it sits
 * behind the same [SensorSource] contract as [PhoneSensorSource] (Phase K).
 *
 * Speed/distance conversion lives HERE, not in the firmware: the ESP32
 * reports raw wheel RPM (see [com.voidroot.bikeos.data.ble.SensorPayload]
 * kdoc), and this class turns that into km/h and accumulated km using the
 * wheel size from [BikeRepository].
 */
class BleSensorSource @Inject constructor(
    private val bleRepository: BleRepository,
    private val bikeRepository: BikeRepository
) : SensorSource {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun stream(): Flow<SensorSnapshot> = bleRepository.connectionState.flatMapLatest { state ->
        if (state is BleConnectionState.Connected) {
            realBleStream()
        } else {
            disconnectedClockStream()
        }
    }

    /** Not fake sensor data - just a real, ticking clock so the clock widget doesn't freeze while disconnected. */
    private fun disconnectedClockStream(): Flow<SensorSnapshot> = flow {
        while (true) {
            emit(SensorSnapshot(currentTime = timeFormat.format(Date()), isConnected = false, source = SensorSourceType.NONE))
            delay(1000)
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
                // dependent), not by the firmware - real per-rider calorie
                // calculation is a follow-up (MET-based formula against
                // UserRepository's weight), kept at 0 for now rather than
                // showing a fabricated number.
                calories = 0,
                cadenceRpm = payload.cadenceRpm,
                batteryPercent = payload.batteryPercent,
                isConnected = true,
                currentTime = timeFormat.format(Date()),
                accelG = payload.accelG,
                source = SensorSourceType.ESP32
            )
        }
    }
}
