package com.voidroot.bikeos

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voidroot.bikeos.core.navigation.BikeOSNavGraph
import com.voidroot.bikeos.core.theme.AppThemeViewModel
import com.voidroot.bikeos.core.theme.BikeOSTheme
import com.voidroot.bikeos.presentation.alarm.AlarmGuard
import com.voidroot.bikeos.presentation.common.RemoteKeyHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single Activity host. All screens are Composable destinations reached
 * through [BikeOSNavGraph]. [AlarmGuard] wraps the whole graph so the
 * anti-theft disarm dialog can appear over any screen, not just Dashboard.
 *
 * enableEdgeToEdge() draws content behind the system status/nav bars with
 * a transparent bar background everywhere - the "unified" look (status bar
 * blends with the screen instead of having its own solid color band) the
 * UI spec asked for. The Dashboard/cluster goes further and hides the
 * system bars entirely - see ImmersiveMode.kt, applied only there.
 *
 * [remoteKeyHandler] (Phase H, keyless starter): the Activity is where
 * Android hands hardware key events from a paired Bluetooth HID remote
 * (the builder's camera-shutter remote) - see RemoteKeyHandler's kdoc for
 * the full reasoning. Deliberately activity-wide (not scoped to just the
 * Dashboard screen) so the remote works regardless of which screen is
 * open, matching how a real ignition button would.
 *
 * installSplashScreen() (Phase I, "million dollar product" first
 * impression) MUST be called before super.onCreate() - it's what makes
 * Theme.BikeOS.Splash (see AndroidManifest.xml + themes.xml) actually
 * show instead of a plain flash. The custom exit animation below fades +
 * scales the splash icon out instead of the default hard cut, closer to
 * how iOS/premium apps transition off their launch screen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var remoteKeyHandler: RemoteKeyHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, "alpha", 1f, 0f)
            val scaleX = ObjectAnimator.ofFloat(splashScreenView.iconView, "scaleX", 1f, 1.15f)
            val scaleY = ObjectAnimator.ofFloat(splashScreenView.iconView, "scaleY", 1f, 1.15f)
            listOf(fadeOut, scaleX, scaleY).forEach {
                it.interpolator = AnticipateInterpolator()
                it.duration = 320L
            }
            fadeOut.doOnEnd { splashScreenView.remove() }
            scaleX.start()
            scaleY.start()
            fadeOut.start()
        }

        setContent {
            val themeViewModel: AppThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()

            BikeOSTheme(isDarkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmGuard {
                        BikeOSNavGraph()
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && remoteKeyHandler.onKeyEvent(keyCode, event)) return true
        return super.onKeyDown(keyCode, event)
    }
}

private fun ObjectAnimator.doOnEnd(action: () -> Unit) {
    addListener(object : android.animation.AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: android.animation.Animator) = action()
    })
}
