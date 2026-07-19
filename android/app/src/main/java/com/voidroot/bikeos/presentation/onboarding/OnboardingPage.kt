package com.voidroot.bikeos.presentation.onboarding

import androidx.compose.ui.graphics.Color

data class OnboardingPage(
    val title: String,
    val description: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val glyph: OnboardingGlyph
)

/** Which simple Canvas icon to draw for this page - see OnboardingGlyphIcon. */
enum class OnboardingGlyph { SPEEDOMETER, BLUETOOTH_LINK, SHIELD_BOLT, PALETTE, BIKE }

val onboardingPages = listOf(
    OnboardingPage(
        title = "Welcome to BikeOS",
        description = "Turn your phone into a full digital cockpit for your bike - mounted on your handlebar, built for riding.",
        gradientStart = Color(0xFF0A0E14),
        gradientEnd = Color(0xFF1B2838),
        glyph = OnboardingGlyph.BIKE
    ),
    OnboardingPage(
        title = "A Real-Time Cluster",
        description = "Speed, cadence, distance, gear, and battery - all in one glanceable, automotive-style dashboard.",
        gradientStart = Color(0xFF00232B),
        gradientEnd = Color(0xFF014455),
        glyph = OnboardingGlyph.SPEEDOMETER
    ),
    OnboardingPage(
        title = "Connects To Your Bike",
        description = "Pairs over Bluetooth with your BikeOS controller for live sensor data straight from the wheel and crank.",
        gradientStart = Color(0xFF1A0033),
        gradientEnd = Color(0xFF3D1466),
        glyph = OnboardingGlyph.BLUETOOTH_LINK
    ),
    OnboardingPage(
        title = "Smart Alerts & Anti-Theft",
        description = "Gear suggestions while you ride, and an alarm that lights up and sounds off if your bike is disturbed while parked.",
        gradientStart = Color(0xFF330A0A),
        gradientEnd = Color(0xFF661616),
        glyph = OnboardingGlyph.SHIELD_BOLT
    ),
    OnboardingPage(
        title = "Make It Yours",
        description = "Pick your widgets, your colors for day and night, your units - BikeOS adapts to how you ride.",
        gradientStart = Color(0xFF1A2E05),
        gradientEnd = Color(0xFF335C0A),
        glyph = OnboardingGlyph.PALETTE
    )
)
