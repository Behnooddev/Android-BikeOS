package com.voidroot.bikeos.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BikeOSDarkColorScheme = darkColorScheme(
    primary = BikePrimary,
    secondary = BikeAccent,
    background = BikeBackground,
    surface = BikeSurface,
    error = BikeDanger,
    onPrimary = BikeBackground,
    onBackground = BikeTextPrimary,
    onSurface = BikeTextPrimary
)

private val BikeOSLightColorScheme = lightColorScheme(
    primary = BikePrimary,
    secondary = BikeAccent,
    background = androidx.compose.ui.graphics.Color(0xFFF5F7FA),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    error = BikeDanger,
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onBackground = androidx.compose.ui.graphics.Color(0xFF0A0E14),
    onSurface = androidx.compose.ui.graphics.Color(0xFF0A0E14)
)

/**
 * App-wide Material theme, driven by the Settings > Theme "Dark theme"
 * toggle (see AppThemeViewModel) - separate from the cluster's own
 * day/night color customization (see ClusterPalette/LocalClusterPalette),
 * which only applies inside the Dashboard screen.
 */
@Composable
fun BikeOSTheme(isDarkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) BikeOSDarkColorScheme else BikeOSLightColorScheme,
        typography = BikeOSTypography,
        content = content
    )
}
