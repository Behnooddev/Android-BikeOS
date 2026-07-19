package com.voidroot.bikeos.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikePrimary

/** Brief branded loading screen while [SplashViewModel] decides where the app should actually start. */
@Composable
fun SplashScreen(navController: NavHostController, viewModel: SplashViewModel = hiltViewModel()) {
    val nextRoute by viewModel.nextRoute.collectAsStateWithLifecycle()

    LaunchedEffect(nextRoute) {
        nextRoute?.let { route ->
            navController.navigate(route) {
                popUpTo(BikeOSDestinations.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BikeBackground),
        contentAlignment = Alignment.Center
    ) {
        Text("BikeOS", style = MaterialTheme.typography.headlineMedium, color = BikePrimary)
    }
}
