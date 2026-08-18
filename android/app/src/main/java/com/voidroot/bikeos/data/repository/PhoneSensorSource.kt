package com.voidroot.bikeos.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Phase K "hardware-free mode": drives the dashboard from the phone's own
 * GPS and accelerometer instead of the ESP32. Deliberately does NOT try to
 * fake or estimate [SensorSnapshot.cadenceRpm] or [SensorSnapshot.batteryPercent] -
 * neither is physically derivable from a phone (no pedal sensor; phone
 * battery isn't bike battery), so they're left at their default (0) and
 * the UI is expected to render them as a permanent dash for
 * [SensorSourceType.PHONE], per the same "never show fake data" rule the
 * ESP32 path follows.
 *
 * GPS speed is noisy at low speed (a stationary phone can jitter to
 * 1-2 km/h from position drift alone), so readings under
 * [MIN_CREDIBLE_SPEED_KMH] are floored to 0 rather than displayed as-is.
 */
class PhoneSensorSource @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorSource {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    override fun stream(): Flow<SensorSnapshot> {
        // No permission yet: don't attempt GPS at all, just tick the clock
        // with source=PHONE and isConnected=false so the UI can show a
        // "grant location" prompt instead of a generic disconnected state.
        if (!hasLocationPermission()) return noPermissionStream()

        var cumulativeDistanceKm = 0f
        var lastFixEpochMs = 0L
        val accelG = MutableStateFlow(0f)

        return combine(locationUpdates(), accelUpdates(accelG)) { location, accel ->
            val rawSpeedKmh = (location.speed * 3.6f) // m/s -> km/h
            val speedKmh = if (rawSpeedKmh < MIN_CREDIBLE_SPEED_KMH) 0f else rawSpeedKmh

            val now = System.currentTimeMillis()
            if (lastFixEpochMs != 0L) {
                val hoursElapsed = (now - lastFixEpochMs) / 3_600_000f
                cumulativeDistanceKm += speedKmh * hoursElapsed
            }
            lastFixEpochMs = now

            SensorSnapshot(
                speedKmh = speedKmh,
                distanceKm = cumulativeDistanceKm,
                calories = 0,
                cadenceRpm = 0,      // physically unavailable from a phone
                batteryPercent = 0,  // this would be phone battery, not bike battery - not the same field
                isConnected = true,
                currentTime = timeFormat.format(Date()),
                accelG = accel,
                source = SensorSourceType.PHONE
            )
        }
    }

    private fun noPermissionStream(): Flow<SensorSnapshot> = flow {
        emit(
            SensorSnapshot(
                currentTime = timeFormat.format(Date()),
                isConnected = false,
                source = SensorSourceType.PHONE
            )
        )
        awaitCancellation()
    }

    private fun locationUpdates(): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL_MS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        // hasLocationPermission() already checked by the caller (stream()),
        // but the compiler can't see that across function boundaries, so
        // this satisfies the @RequiresPermission contract on requestLocationUpdates.
        if (hasLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(request, callback, context.mainLooper)
        } else {
            close()
        }

        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    /** Emits magnitude of the phone's accelerometer in g, same unit as the ESP32's MPU6050 field. */
    private fun accelUpdates(target: MutableStateFlow<Float>): Flow<Float> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            trySend(0f)
            awaitClose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val (x, y, z) = event.values
                    val magnitudeG = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                    trySend(magnitudeG)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }.distinctUntilChanged()

    private companion object {
        const val LOCATION_INTERVAL_MS = 1000L
        const val MIN_CREDIBLE_SPEED_KMH = 2f
    }
}
