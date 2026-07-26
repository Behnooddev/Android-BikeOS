package com.voidroot.bikeos.presentation.menu.calculator

enum class CalculatorMode(val label: String) {
    SPEED_DISTANCE_TIME("Speed / Distance / Time"),
    GEAR_RATIO("Gear Ratio"),
    CALORIES("Calories")
}

/** Which of the three Speed/Distance/Time values the user wants computed from the other two. */
enum class SolveFor(val label: String) {
    SPEED("Speed"), DISTANCE("Distance"), TIME("Time")
}
