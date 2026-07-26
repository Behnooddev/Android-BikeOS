package com.voidroot.bikeos.presentation.menu.calculator

data class CalculatorUiState(
    val mode: CalculatorMode = CalculatorMode.SPEED_DISTANCE_TIME,

    // Speed/Distance/Time
    val solveFor: SolveFor = SolveFor.SPEED,
    val distanceKm: String = "",
    val timeMinutes: String = "",
    val speedKmh: String = "",

    // Gear Ratio
    val frontTeeth: String = "",
    val rearTeeth: String = "",
    val wheelSizeInches: String = "27.5",
    val cadenceRpm: String = "90",

    // Calories
    val caloriesSpeedKmh: String = "20",
    val caloriesWeightKg: String = "",
    val caloriesDurationMinutes: String = "30",

    val resultTitle: String? = null,
    val resultValue: String? = null,
    val reaction: String? = null,
    val errorMessage: String? = null
)
