package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeWarning
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import com.voidroot.bikeos.presentation.dashboard.LightState

/**
 * The Bike Control Panel's light toggles (per the UI/UX spec) - laid out
 * as a vertical stack of icon cards (matching the cockpit's right-side
 * control column) rather than a horizontal pill row. Large touch targets
 * on purpose - this needs to be usable with a gloved thumb while riding,
 * not precision tapping.
 *
 * State shown here is [LightState] - Android's optimistic local record of
 * what it last asked the ESP32 for, not a confirmed physical readback (the
 * firmware doesn't send one back). See LightState's kdoc for why.
 */
@Composable
fun LightControlRow(
    lightState: LightState,
    onToggleFront: () -> Unit,
    onToggleRear: () -> Unit,
    onToggleBody: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LightToggleCard("Front", Icons.Filled.Lightbulb, lightState.front, onToggleFront)
        LightToggleCard("Rear", Icons.Filled.WbIncandescent, lightState.rear, onToggleRear)
        LightToggleCard("Body", Icons.Filled.DirectionsBike, lightState.body, onToggleBody)
    }
}

@Composable
private fun LightToggleCard(label: String, icon: ImageVector, isOn: Boolean, onToggle: () -> Unit) {
    val palette = LocalClusterPalette.current
    val ringColor by animateColorAsState(
        targetValue = if (isOn) BikeWarning else palette.textSecondary.copy(alpha = 0.3f),
        label = "lightRingColor"
    )
    val glowBackground by animateColorAsState(
        targetValue = if (isOn) BikeWarning.copy(alpha = 0.18f) else Color.Transparent,
        label = "lightGlowBackground"
    )

    GlassCard(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onToggle)
    ) {
        Column(
            modifier = Modifier.width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(glowBackground, CircleShape)
                    .border(1.dp, ringColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = if (isOn) BikeWarning else palette.textSecondary, modifier = Modifier.size(20.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOn) palette.textPrimary else palette.textSecondary
            )
        }
    }
}
