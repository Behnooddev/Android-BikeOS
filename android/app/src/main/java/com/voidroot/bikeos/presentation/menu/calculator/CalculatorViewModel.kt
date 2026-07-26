package com.voidroot.bikeos.presentation.menu.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.core.health.CalorieCalculator
import com.voidroot.bikeos.core.health.GearRatioCalculator
import com.voidroot.bikeos.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * A bike-specific calculator with three modes (per the product spec: pick
 * what to calculate, give it inputs, get a result AND a short reaction
 * comment based on the result's magnitude - not just a bare number).
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Prefill weight from the rider's profile once (still fully
        // editable afterward, for a "what if I weighed X" hypothetical) -
        // one less thing to type before the very first calculation.
        viewModelScope.launch {
            userRepository.observe().collect { user ->
                if (_uiState.value.caloriesWeightKg.isEmpty() && user.weightKg > 0) {
                    _uiState.value = _uiState.value.copy(caloriesWeightKg = user.weightKg.toString())
                }
            }
        }
    }

    fun setMode(mode: CalculatorMode) {
        _uiState.value = _uiState.value.copy(mode = mode, resultTitle = null, resultValue = null, reaction = null, errorMessage = null)
    }

    fun setSolveFor(solveFor: SolveFor) {
        _uiState.value = _uiState.value.copy(solveFor = solveFor, resultTitle = null, resultValue = null, reaction = null, errorMessage = null)
    }

    fun update(transform: (CalculatorUiState) -> CalculatorUiState) {
        _uiState.value = transform(_uiState.value).copy(errorMessage = null)
    }

    fun calculate() {
        val state = _uiState.value
        when (state.mode) {
            CalculatorMode.SPEED_DISTANCE_TIME -> calculateSpeedDistanceTime(state)
            CalculatorMode.GEAR_RATIO -> calculateGearRatio(state)
            CalculatorMode.CALORIES -> calculateCalories(state)
        }
    }

    private fun calculateSpeedDistanceTime(state: CalculatorUiState) {
        val distance = state.distanceKm.toFloatOrNull()
        val time = state.timeMinutes.toFloatOrNull()
        val speed = state.speedKmh.toFloatOrNull()

        val (title, value, reaction) = when (state.solveFor) {
            SolveFor.SPEED -> {
                if (distance == null || time == null || time <= 0f) return showError("Enter distance and time")
                val result = distance / (time / 60f)
                Triple("Speed", "%.1f km/h".format(result), speedReaction(result))
            }
            SolveFor.DISTANCE -> {
                if (speed == null || time == null) return showError("Enter speed and time")
                val result = speed * (time / 60f)
                Triple("Distance", "%.1f km".format(result), distanceReaction(result))
            }
            SolveFor.TIME -> {
                if (distance == null || speed == null || speed <= 0f) return showError("Enter distance and speed")
                val result = distance / speed * 60f
                Triple("Time", "%d min".format(result.roundToInt()), timeReaction(result))
            }
        }
        showResult(title, value, reaction)
    }

    private fun calculateGearRatio(state: CalculatorUiState) {
        val front = state.frontTeeth.toIntOrNull()
        val rear = state.rearTeeth.toIntOrNull()
        val wheelSize = state.wheelSizeInches.toFloatOrNull()
        val cadence = state.cadenceRpm.toFloatOrNull()

        if (front == null || rear == null || rear <= 0 || wheelSize == null) {
            return showError("Enter front/rear teeth and wheel size")
        }

        val development = GearRatioCalculator.developmentMeters(front, rear, wheelSize)
        val gearInches = GearRatioCalculator.gearInches(front, rear, wheelSize)
        val speedAtCadence = cadence?.let { GearRatioCalculator.speedAtCadence(front, rear, wheelSize, it) }

        val value = buildString {
            append("%.2f m/pedal-rev - %.1f gear-in".format(development, gearInches))
            if (speedAtCadence != null) append(" - %.1f km/h at %.0f rpm".format(speedAtCadence, cadence))
        }

        showResult("Gear Ratio", value, gearReaction(gearInches))
    }

    private fun calculateCalories(state: CalculatorUiState) {
        val speed = state.caloriesSpeedKmh.toFloatOrNull()
        val weight = state.caloriesWeightKg.toIntOrNull()
        val duration = state.caloriesDurationMinutes.toFloatOrNull()

        if (speed == null || weight == null || weight <= 0 || duration == null || duration <= 0f) {
            return showError("Enter speed, weight, and duration")
        }

        val calories = CalorieCalculator.estimateCalories(speed, weight, duration)
        showResult("Calories", "${calories.roundToInt()} kcal", caloriesReaction(calories))
    }

    private fun showResult(title: String, value: String, reaction: String) {
        _uiState.value = _uiState.value.copy(resultTitle = title, resultValue = value, reaction = reaction, errorMessage = null)
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(resultTitle = null, resultValue = null, reaction = null, errorMessage = message)
    }

    // ---- Reaction comments - the "and give a reaction based on the output" part of the spec ----

    private fun speedReaction(kmh: Float) = when {
        kmh > 40f -> "That's race-level fast for a bike!"
        kmh > 25f -> "Solid pace - well above average cruising speed."
        kmh > 15f -> "A nice, steady cruising speed."
        else -> "Nice and easy."
    }

    private fun distanceReaction(km: Float) = when {
        km > 100f -> "That's a serious long-distance ride!"
        km > 50f -> "That's a solid distance - a proper long ride."
        km > 15f -> "A good, satisfying ride length."
        else -> "A short, quick spin."
    }

    private fun timeReaction(minutes: Float) = when {
        minutes > 180f -> "That's a multi-hour endurance ride."
        minutes > 60f -> "Over an hour in the saddle - respectable."
        else -> "A quick ride."
    }

    private fun gearReaction(gearInches: Float) = when {
        gearInches > 90f -> "A hard gear - great for flat sprints, tough on climbs."
        gearInches > 60f -> "A balanced, all-purpose gear."
        else -> "An easy gear - great for climbing."
    }

    private fun caloriesReaction(calories: Float) = when {
        calories > 800f -> "Big calorie burn - that's a serious workout!"
        calories > 400f -> "A solid workout."
        else -> "A light, easy effort."
    }
}
