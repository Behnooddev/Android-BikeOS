package com.voidroot.bikeos.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voidroot.bikeos.presentation.dashboard.DashboardScreen
import com.voidroot.bikeos.presentation.menu.about.AboutScreen
import com.voidroot.bikeos.presentation.menu.account.AccountScreen
import com.voidroot.bikeos.presentation.menu.appearance.AppearanceScreen
import com.voidroot.bikeos.presentation.menu.calculator.CalculatorScreen
import com.voidroot.bikeos.presentation.menu.home.HomeScreen
import com.voidroot.bikeos.presentation.menu.settings.SettingsScreen
import com.voidroot.bikeos.presentation.onboarding.OnboardingScreen
import com.voidroot.bikeos.presentation.signup.SignupScreen
import com.voidroot.bikeos.presentation.splash.SplashScreen

/**
 * Startup flow: SPLASH (reads AppState once) -> ONBOARDING (first run only)
 * -> SIGNUP (first run only) -> MENU_HOME (permanent landing screen from
 * then on). DASHBOARD (the cluster) is only reached by pressing Start on
 * Home, and its own Exit button (see DashboardScreen) pops back to Home.
 *
 * Erase Data (Settings) clears AppState too, so Splash routes back through
 * Onboarding/Signup again next launch - see SettingsViewModel.eraseAllData.
 */
@Composable
fun BikeOSNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = BikeOSDestinations.SPLASH) {
        composable(BikeOSDestinations.SPLASH) { SplashScreen(navController) }
        composable(BikeOSDestinations.ONBOARDING) { OnboardingScreen(navController) }
        composable(BikeOSDestinations.SIGNUP) { SignupScreen(navController) }

        composable(BikeOSDestinations.DASHBOARD) { DashboardScreen(navController) }
        composable(BikeOSDestinations.MENU_HOME) { HomeScreen(navController) }
        composable(BikeOSDestinations.MENU_APPEARANCE) { AppearanceScreen() }
        composable(BikeOSDestinations.MENU_CALCULATOR) { CalculatorScreen() }
        composable(BikeOSDestinations.MENU_SETTINGS) { SettingsScreen(navController) }
        composable(BikeOSDestinations.MENU_ACCOUNT) { AccountScreen() }
        composable(BikeOSDestinations.MENU_ABOUT) { AboutScreen() }
    }
}
