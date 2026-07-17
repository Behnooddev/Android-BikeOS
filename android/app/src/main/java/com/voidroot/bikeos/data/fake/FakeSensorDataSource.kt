package com.voidroot.bikeos.data.fake

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Raw values a real ESP32 Sensor Data Service will eventually push over BLE.
 * Kept separate from [DashboardUiState] on purpose: this is "what hardware
 * reports", the UI state also carries app-local concerns (ride mode, gear
 * selection) that don't come from the sensor stream.
 */
data class SensorSnapshot(
    val speedKmh: Float,
    val distanceKm: Float,
    val calories: Int,
    val cadenceRpm: Int,
    val batteryPercent: Int,
    val isConnected: Boolean,
    val currentTime: String,
    // 0 = "no target detected", matching the firmware's VL53L1X convention
    // (see sensors.h). Not consumed by any UI yet - the Alert System that
    // will threshold this into a warning is a later phase.
    val rearDistanceMm: Int = 0
)

/**
 * Phase 1 stand-in for the real BLE repository (arrives Phase 3).
 *
 * Simulates a rider accelerating/braking with a clamped random walk rather
 * than pure noise, so the speedometer has something realistic to animate
 * smoothly toward - matching the "no instant changes, smooth interpolation"
 * requirement from the UI spec.
 */
object FakeSensorDataSource {
    private const val TICK_MS = 200L
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun stream(): Flow<SensorSnapshot> = flow {
        var speed = 0f
        var distanceKm = 0f
        var calories = 0f
        var battery = 100f
        var tick = 0L

        while (true) {
            val delta = Random.nextFloat() * 3f - 1.4f
            speed = (speed + delta).coerceIn(0f, 45f)

            val hoursPerTick = TICK_MS / 3_600_000f
            distanceKm += speed * hoursPerTick
            calories += speed * 0.08f

            val cadence = if (speed > 1f) {
                (60f + speed * 1.3f + Random.nextInt(-4, 4)).coerceIn(0f, 120f)
            } else 0f

            if (tick % 150L == 0L && tick > 0L) {
                battery = (battery - 1f).coerceAtLeast(0f)
            }

            // simulate the ~3s BLE "found + connected" delay on cold start
            val isConnected = tick >= 15L

            emit(
                SensorSnapshot(
                    speedKmh = speed,
                    distanceKm = distanceKm,
                    calories = calories.toInt(),
                    cadenceRpm = cadence.toInt(),
                    batteryPercent = battery.toInt(),
                    isConnected = isConnected,
                    currentTime = timeFormat.format(Date())
                )
            )

            tick++
            delay(TICK_MS)
        }
    }
}
