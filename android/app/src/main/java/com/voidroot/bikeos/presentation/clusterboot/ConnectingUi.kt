package com.voidroot.bikeos.presentation.clusterboot

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

/**
 * Shown while [ClusterBootScreen] is attempting the BLE connection. After a
 * timeout with no connection, [showContinueAnyway] flips true and a
 * fallback button appears - the rider is never stuck waiting forever for
 * hardware that isn't there.
 */
@Composable
fun ConnectingUi(showContinueAnyway: Boolean, onContinueAnyway: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "connectingPulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Box(modifier = Modifier.fillMaxSize().background(BikeBackground), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .background(BikePrimary.copy(alpha = 0.3f), CircleShape)
            )
            Text(
                "Connecting to your bike...",
                style = MaterialTheme.typography.titleMedium,
                color = BikeTextPrimary,
                modifier = Modifier.padding(top = 24.dp)
            )
            if (showContinueAnyway) {
                Text(
                    "Couldn't find your BikeOS device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BikeTextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
                Button(onClick = onContinueAnyway) {
                    Text("Continue without connection")
                }
            }
        }
    }
}
