package com.voidroot.bikeos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.voidroot.bikeos.core.navigation.BikeOSNavGraph
import com.voidroot.bikeos.core.theme.BikeOSTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity host. All screens are Composable destinations reached
 * through [BikeOSNavGraph] - no fragment-based or multi-Activity navigation.
 *
 * @AndroidEntryPoint is required (Phase 2) for hiltViewModel() to work in
 * any Composable reached from this Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BikeOSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BikeOSNavGraph()
                }
            }
        }
    }
}
