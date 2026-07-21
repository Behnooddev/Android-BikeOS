package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeSuccess
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import com.voidroot.bikeos.data.calls.IncomingCall

/**
 * Incoming-call banner - only visible while a call is actually ringing
 * (unlike the other widgets, which are always-on if enabled). Answer/Reject
 * are handled by the handlebar's Gear Up/Gear Down buttons (see
 * DashboardViewModel), so this widget is informational + a reminder of
 * which button does what, not something the rider needs to tap.
 */
@Composable
fun CallWidget(incomingCall: IncomingCall?, modifier: Modifier = Modifier) {
    val palette = LocalClusterPalette.current

    AnimatedVisibility(visible = incomingCall != null, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        incomingCall?.let { call ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "Incoming call",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                    Text(
                        call.contactName ?: call.phoneNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Gear Up = Answer", style = MaterialTheme.typography.labelSmall, color = BikeSuccess)
                        Text("Gear Down = Reject", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                    }
                }
            }
        }
    }
}
