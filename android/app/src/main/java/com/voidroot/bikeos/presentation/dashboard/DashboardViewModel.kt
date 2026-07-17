package com.voidroot.bikeos.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.ble.ControlCommand
import com.voidroot.bikeos.data.ble.DeviceButtonEvent
import com.voidroot.bikeos.data.repository.BikeRepository
import com.voidroot.bikeos.data.repository.BleRepository
import com.voidroot.bikeos.data.repository.DashboardConfigRepository
import com.voidroot.bikeos.data.repository.RideRepository
import com.voidroot.bikeos.data.repository.RideSession
import com.voidroot.bikeos.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Local, optimistic light toggle state for the Bike Control Panel.
 *
 * Deliberately NOT part of [DashboardUiState]/the main combine() - lights
 * are fire-and-forget BLE writes (the firmware doesn't send back a light-
 * state confirmation), and the physical Light button on the handlebar can
 * also change the real state independently. This is a best-effort local
 * mirror of "what Android last asked for", not a guaranteed reflection of
 * the physical light state - same "two control paths, last write wins"
 * reality documented in the firmware's controls.h.
 */
data class LightState(
    val front: Boolean = false,
    val rear: Boolean = false,
    val body: Boolean = false
)

/**
 * Internal, in-memory ride aggregate. Deliberately NOT persisted per-tick
 * (see [RideSessionEntity][com.voidroot.bikeos.data.local.entity.RideSessionEntity]
 * kdoc) - only [stopRide] writes anything to Room, once.
 */
private data class RideAccumulator(
    val startTimeEpochMs: Long = 0L,
    val distanceAtStartKm: Float = 0f,
    val caloriesAtStart: Int = 0,
    val speedSum: Float = 0f,
    val speedSamples: Int = 0,
    val maxSpeedKmh: Float = 0f,
    val cadenceSum: Int = 0,
    val cadenceSamples: Int = 0,
    val maxCadenceRpm: Int = 0
)

/**
 * Combines [SensorRepository]'s stream (real BLE data when connected, the
 * Phase 1 fake generator otherwise) with Room-backed bike/widget state and
 * local ride-mode/ride-active selections into one [DashboardUiState].
 *
 * Also listens for handlebar Button Events (Mode/Gear Up/Gear Down) coming
 * over BLE - the ESP32 only reports that a button was pressed, this
 * ViewModel decides what the next mode/gear actually is (see controls.h's
 * kdoc on the firmware side for why that split exists).
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val bikeRepository: BikeRepository,
    private val dashboardConfigRepository: DashboardConfigRepository,
    private val rideRepository: RideRepository,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _rideMode = MutableStateFlow(RideMode.CRUISE)
    private val _isRideActive = MutableStateFlow(false)

    // Mutated from within the combine() collector below only - single
    // collector coroutine, so no concurrent-write concern despite being a
    // plain var rather than a StateFlow.
    private var accumulator = RideAccumulator()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _lightState = MutableStateFlow(LightState())
    val lightState: StateFlow<LightState> = _lightState.asStateFlow()

    init {
        viewModelScope.launch { dashboardConfigRepository.ensureSeeded() }

        viewModelScope.launch {
            bleRepository.buttonEvents.collect { event -> onDeviceButtonEvent(event) }
        }

        viewModelScope.launch {
            combine(
                sensorRepository.stream(),
                _rideMode,
                bikeRepository.observe(),
                dashboardConfigRepository.observeWidgets(),
                _isRideActive
            ) { snapshot, mode, bike, widgets, active ->
                if (active) {
                    accumulator = accumulator.copy(
                        speedSum = accumulator.speedSum + snapshot.speedKmh,
                        speedSamples = accumulator.speedSamples + 1,
                        maxSpeedKmh = maxOf(accumulator.maxSpeedKmh, snapshot.speedKmh),
                        cadenceSum = accumulator.cadenceSum + snapshot.cadenceRpm,
                        cadenceSamples = accumulator.cadenceSamples + 1,
                        maxCadenceRpm = maxOf(accumulator.maxCadenceRpm, snapshot.cadenceRpm)
                    )
                }

                DashboardUiState(
                    speedKmh = snapshot.speedKmh,
                    distanceKm = snapshot.distanceKm,
                    calories = snapshot.calories,
                    cadenceRpm = snapshot.cadenceRpm,
                    batteryPercent = snapshot.batteryPercent,
                    isConnected = snapshot.isConnected,
                    frontGear = bike.currentFrontGear,
                    rearGear = bike.currentRearGear,
                    rideMode = mode,
                    currentTime = snapshot.currentTime,
                    enabledWidgetKeys = widgets.filter { it.enabled }.map { it.key }.toSet(),
                    isRideActive = active
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onRideModeSelected(mode: RideMode) {
        _rideMode.value = mode
    }

    fun toggleFrontLight() {
        val next = !_lightState.value.front
        _lightState.value = _lightState.value.copy(front = next)
        bleRepository.sendCommand(if (next) ControlCommand.FRONT_LIGHT_ON else ControlCommand.FRONT_LIGHT_OFF)
    }

    fun toggleRearLight() {
        val next = !_lightState.value.rear
        _lightState.value = _lightState.value.copy(rear = next)
        bleRepository.sendCommand(if (next) ControlCommand.REAR_LIGHT_ON else ControlCommand.REAR_LIGHT_OFF)
    }

    fun toggleBodyLight() {
        val next = !_lightState.value.body
        _lightState.value = _lightState.value.copy(body = next)
        bleRepository.sendCommand(if (next) ControlCommand.BODY_LIGHT_ON else ControlCommand.BODY_LIGHT_OFF)
    }

    private fun onDeviceButtonEvent(event: DeviceButtonEvent) {
        when (event) {
            DeviceButtonEvent.MODE -> {
                val modes = RideMode.entries
                val nextIndex = (modes.indexOf(_rideMode.value) + 1) % modes.size
                _rideMode.value = modes[nextIndex]
            }
            DeviceButtonEvent.GEAR_UP -> viewModelScope.launch {
                val state = _uiState.value
                bikeRepository.syncCurrentGear(state.frontGear, state.rearGear + 1)
            }
            DeviceButtonEvent.GEAR_DOWN -> viewModelScope.launch {
                val state = _uiState.value
                bikeRepository.syncCurrentGear(state.frontGear, state.rearGear - 1)
            }
        }
    }

    fun startRide() {
        if (_isRideActive.value) return
        accumulator = RideAccumulator(
            startTimeEpochMs = System.currentTimeMillis(),
            distanceAtStartKm = _uiState.value.distanceKm,
            caloriesAtStart = _uiState.value.calories
        )
        _isRideActive.value = true
    }

    fun stopRide() {
        if (!_isRideActive.value) return
        val acc = accumulator
        val state = _uiState.value
        val endTime = System.currentTimeMillis()

        _isRideActive.value = false

        viewModelScope.launch {
            rideRepository.saveCompletedRide(
                RideSession(
                    startTimeEpochMs = acc.startTimeEpochMs,
                    endTimeEpochMs = endTime,
                    durationSeconds = ((endTime - acc.startTimeEpochMs) / 1000).coerceAtLeast(0),
                    distanceKm = (state.distanceKm - acc.distanceAtStartKm).coerceAtLeast(0f),
                    calories = (state.calories - acc.caloriesAtStart).coerceAtLeast(0),
                    avgSpeedKmh = if (acc.speedSamples > 0) acc.speedSum / acc.speedSamples else 0f,
                    maxSpeedKmh = acc.maxSpeedKmh,
                    avgCadenceRpm = if (acc.cadenceSamples > 0) acc.cadenceSum / acc.cadenceSamples else 0,
                    maxCadenceRpm = acc.maxCadenceRpm,
                    rideMode = state.rideMode.name
                )
            )
        }
    }
}
