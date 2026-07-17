package com.voidroot.bikeos.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BikeOSColorScheme = darkColorScheme(
    primary = BikePrimary,
    secondary = BikeAccent,
    background = BikeBackground,
    surface = BikeSurface,
    error = BikeDanger,
    onPrimary = BikeBackground,
    onBackground = BikeTextPrimary,
    onSurface = BikeTextPrimary
)

/**
 * BikeOS is dark-only by product direction (cockpit UI, outdoor readability).
 * System dark-mode preference is intentionally not consulted - dark mode is
 * a fixed product decision here, not a system-preference follow.
 */
@Composable
fun BikeOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BikeOSColorScheme,
        typography = BikeOSTypography,
        content = content
    )
}
