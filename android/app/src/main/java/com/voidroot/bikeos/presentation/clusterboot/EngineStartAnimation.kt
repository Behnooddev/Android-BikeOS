package com.voidroot.bikeos.presentation.clusterboot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.presentation.dashboard.components.SpeedGauge
import kotlinx.coroutines.delay

private const val MAX_SWEEP_SPEED = 45f

/**
 * The "engine catching" boot sequence: the speed gauge sweeps 0 -> max ->
 * back to 0, with a tone + haptic pulse timed to the moment it's at full
 * sweep (see EngineStartFeedback.kt) - a car-dashboard-style self-test,
 * reusing the real [SpeedGauge] component rather than a separate animation
 * (it already animates toward whatever target it's given via a spring, so
 * scripting a sequence of targets is enough).
 *
 * If disabled in Settings, skips straight to [onFinished] with no delay -
 * this composable is still mounted either way so the caller doesn't need
 * an if/else at the call site.
 */
@Composable
fun EngineStartAnimation(animationEnabled: Boolean, onFinished: () -> Unit) {
    var targetSpeed by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    LaunchedEffect(animationEnabled) {
        if (!animationEnabled) {
            onFinished()
            return@LaunchedEffect
        }

        targetSpeed = MAX_SWEEP_SPEED
        delay(900)
        playEngineStartFeedback(context)
        delay(300)
        targetSpeed = 0f
        delay(900)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize().background(BikeBackground), contentAlignment = Alignment.Center) {
        SpeedGauge(speedKmh = targetSpeed, maxSpeedKmh = MAX_SWEEP_SPEED)
    }
}
