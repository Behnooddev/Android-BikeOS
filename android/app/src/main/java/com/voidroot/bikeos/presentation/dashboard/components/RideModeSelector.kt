package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import com.voidroot.bikeos.presentation.dashboard.RideMode

/**
 * Horizontal row of ride-mode chips. Selection is instant + animated color
 * feedback - recommendation messages tied to mode changes (per the Alert
 * System spec) arrive with the alert system in a later phase.
 */
@Composable
fun RideModeSelector(
    selected: RideMode,
    onSelect: (RideMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalClusterPalette.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RideMode.entries.forEach { mode ->
            val isSelected = mode == selected
            val background by animateColorAsState(
                targetValue = if (isSelected) mode.color.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent,
                label = "modeChipBackground"
            )
            val border by animateColorAsState(
                targetValue = if (isSelected) mode.color else palette.textSecondary.copy(alpha = 0.3f),
                label = "modeChipBorder"
            )

            Row(
                modifier = Modifier
                    .background(background, RoundedCornerShape(50))
                    .border(1.dp, border, RoundedCornerShape(50))
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) palette.textPrimary else palette.textSecondary
                )
            }
        }
    }
}
