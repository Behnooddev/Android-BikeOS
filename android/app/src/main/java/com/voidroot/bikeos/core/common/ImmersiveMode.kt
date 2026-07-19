package com.voidroot.bikeos.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Hides the system status bar + navigation bar for as long as the calling
 * composable is in the composition, and restores them on dispose. Used
 * ONLY by the Dashboard/cluster screen - per the UI spec, the cockpit
 * should be a true fullscreen instrument, not a normal app screen; every
 * other screen keeps the (edge-to-edge, unified-color) system bars.
 *
 * Swiping from an edge still temporarily reveals the bars (BEHAVIOR_SHOW_BARS_BY_SWIPE) -
 * intentional, so the rider isn't ever truly locked out of exiting.
 */
@Composable
fun ImmersiveMode() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            if (window != null) {
                WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}
