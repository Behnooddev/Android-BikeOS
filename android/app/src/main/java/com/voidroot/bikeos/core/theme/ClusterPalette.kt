package com.voidroot.bikeos.core.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The cluster's own color set - independent from the app-wide dark/light
 * theme (see BikeOSTheme). Per the product spec, users can customize
 * "every color inside the cluster" with separate Day and Night palettes
 * (auto-selected by time of day, not a manual toggle - see
 * ClusterPaletteResolver). Provided via [LocalClusterPalette] so Dashboard's
 * components read it without every composable needing an explicit param.
 */
data class ClusterPalette(
    val primary: Color,
    val accent: Color,
    val background: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

val DefaultClusterPalette = ClusterPalette(
    primary = BikePrimary,
    accent = BikeAccent,
    background = BikeBackground,
    cardBackground = BikeGlassTint,
    cardBorder = BikeGlassBorder,
    textPrimary = BikeTextPrimary,
    textSecondary = BikeTextSecondary
)

val LocalClusterPalette = compositionLocalOf { DefaultClusterPalette }
