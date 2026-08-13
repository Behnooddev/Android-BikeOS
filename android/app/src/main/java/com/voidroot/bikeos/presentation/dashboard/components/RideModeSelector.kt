package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import com.voidroot.bikeos.presentation.dashboard.RideMode

private fun modeIcon(mode: RideMode): ImageVector = when (mode) {
    RideMode.ECO -> Icons.Filled.Cloud
    RideMode.CRUISE -> Icons.Filled.RadioButtonChecked
    RideMode.SPRINT -> Icons.Filled.Bolt
    RideMode.CLIMB -> Icons.Filled.Terrain
    RideMode.DOWNHILL -> Icons.Filled.ArrowDownward
}

/**
 * Ride-mode pill row - each chip carries the mode's glyph + label, with a
 * gentle scale-up and glowing border when selected instead of just a flat
 * background swap, for a more premium feel than a plain filter-chip look.
 */
@Composable
fun RideModeSelector(
    selected: RideMode,
    onSelect: (RideMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalClusterPalette.current

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RideMode.entries.forEach { mode ->
            val isSelected = mode == selected
            val scale by animateFloatAsState(if (isSelected) 1.08f else 1f, label = "modeChipScale")
            val background by animateColorAsState(
                targetValue = if (isSelected) mode.color.copy(alpha = 0.22f) else Color.Transparent,
                label = "modeChipBackground"
            )
            val border by animateColorAsState(
                targetValue = if (isSelected) mode.color else palette.textSecondary.copy(alpha = 0.25f),
                label = "modeChipBorder"
            )

            Row(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(background, RoundedCornerShape(50))
                    .border(if (isSelected) 1.5.dp else 1.dp, border, RoundedCornerShape(50))
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    modeIcon(mode),
                    contentDescription = null,
                    tint = if (isSelected) mode.color else palette.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) palette.textPrimary else palette.textSecondary
                )
            }
        }
    }
}
