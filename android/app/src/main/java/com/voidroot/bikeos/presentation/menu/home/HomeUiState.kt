package com.voidroot.bikeos.presentation.menu.home

data class HomeUiState(
    val firstName: String = "",
    val greetingMessage: String = "",
    val totalDistanceKm: Float = 0f,
    val ridingStyleSummary: String = "Not enough data yet - go for a ride!"
)
