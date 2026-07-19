package com.voidroot.bikeos.core.theme

import androidx.compose.ui.graphics.Color
import com.voidroot.bikeos.data.repository.ThemeColors
import com.voidroot.bikeos.data.repository.ThemePalette
import java.util.Calendar

/**
 * Day = 6:00-18:00 local time, Night = otherwise. Simple clock-based split
 * rather than sunrise/sunset lookup (which would need location permission
 * just for a color choice) - a reasonable v1 per the product spec's
 * "day... night... better visibility" framing.
 */
fun resolveClusterPalette(colors: ThemeColors): ClusterPalette {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isDay = hour in 6 until 18
    val palette = if (isDay) colors.day else colors.night
    return palette.toClusterPalette()
}

private fun ThemePalette.toClusterPalette() = ClusterPalette(
    primary = Color(primary.toInt()),
    accent = Color(accent.toInt()),
    background = Color(background.toInt()),
    cardBackground = Color(cardBackground.toInt()),
    cardBorder = Color(cardBackground.toInt()).copy(alpha = 0.6f),
    textPrimary = Color(textPrimary.toInt()),
    textSecondary = Color(textPrimary.toInt()).copy(alpha = 0.7f)
)
