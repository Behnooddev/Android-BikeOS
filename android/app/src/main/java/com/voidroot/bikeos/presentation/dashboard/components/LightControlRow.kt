package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.core.theme.BikeWarning
import com.voidroot.bikeos.presentation.dashboard.LightState

/**
 * The Bike Control Panel's light toggles (per the UI/UX spec). Large
 * touch targets on purpose - this needs to be usable with a gloved thumb
 * while riding, not precision tapping.
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
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LightToggleChip("Front", lightState.front, onToggleFront)
        LightToggleChip("Rear", lightState.rear, onToggleRear)
        LightToggleChip("Body", lightState.body, onToggleBody)
    }
}

@Composable
private fun LightToggleChip(label: String, isOn: Boolean, onToggle: () -> Unit) {
    val background by animateColorAsState(
        targetValue = if (isOn) BikeWarning.copy(alpha = 0.25f) else Color.Transparent,
        label = "lightChipBackground"
    )
    val border by animateColorAsState(
        targetValue = if (isOn) BikeWarning else BikeTextSecondary.copy(alpha = 0.3f),
        label = "lightChipBorder"
    )

    Row(
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onToggle)
            .padding(horizontal = 18.dp, vertical = 12.dp) // large touch target
    ) {
        Text(
            text = (if (isOn) "\u2600 " else "\u25CB ") + label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isOn) BikeTextPrimary else BikeTextSecondary
        )
    }
}
