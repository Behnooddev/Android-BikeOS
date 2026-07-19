package com.voidroot.bikeos.core.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.LocalClusterPalette

/**
 * Shared glassmorphism container - reads [LocalClusterPalette] so it
 * automatically reflects the cluster's day/night color customization
 * wherever it's provided (Dashboard), and falls back to the default app
 * palette everywhere else (Home, Settings, etc).
 *
 * True backdrop blur (blurring whatever is visually BEHIND the card) isn't
 * something Compose supports directly without capturing/re-rendering the
 * layer behind it, which is a much bigger change for a marginal visual
 * gain here. This uses a subtle diagonal gradient fill + soft shadow +
 * thin border instead, which reads as "glass" without that complexity.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalClusterPalette.current
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = shape, ambientColor = palette.primary.copy(alpha = 0.15f))
            .background(
                Brush.linearGradient(
                    listOf(palette.cardBackground.copy(alpha = 0.28f), palette.cardBackground.copy(alpha = 0.12f))
                ),
                shape
            )
            .border(1.dp, palette.cardBorder, shape)
            .padding(16.dp)
    ) {
        content()
    }
}
