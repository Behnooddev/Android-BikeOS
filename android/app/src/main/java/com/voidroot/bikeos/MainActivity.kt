package com.voidroot.bikeos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voidroot.bikeos.core.navigation.BikeOSNavGraph
import com.voidroot.bikeos.core.theme.AppThemeViewModel
import com.voidroot.bikeos.core.theme.BikeOSTheme
import com.voidroot.bikeos.presentation.alarm.AlarmGuard
import dagger.hilt.android.AndroidEntryPoint

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
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
}
