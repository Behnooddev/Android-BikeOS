package com.voidroot.bikeos.core.common

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Forces landscape orientation for as long as the calling composable is in
 * the composition, restoring the previous orientation setting on dispose.
 *
 * The app is portrait by default (manifest: screenOrientation="portrait")
 * per the product ask - only the cluster experience (Dashboard, and the
 * ClusterBoot connecting/animation screens that lead into it) needs
 * landscape, since that's when the phone is mounted sideways on the
 * handlebar. Every other screen (Home, Settings, Signup, etc.) stays
 * portrait, which is what a phone held in the hand actually wants.
 */
@Composable
fun LandscapeOnly() {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val activity = view.context as? Activity
        val previousOrientation = activity?.requestedOrientation

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}
