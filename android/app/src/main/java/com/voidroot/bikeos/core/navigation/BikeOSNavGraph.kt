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

/**
 * Phase 0 nav graph: the Dashboard is the start destination (per product
 * spec - the rider should land directly on the cockpit, not a menu). Menu
 * screens are reachable but contain only placeholder content until their
 * respective phases (mainly Phase 2 for Settings/data-backed screens).
 *
 * NOTE: no "Riding State" gating yet (see architecture review, item #3 -
 * Riding State is a Phase 1/2 concern once real speed data exists). This
 * graph will later wrap menu routes with a guard once that state exists.
 */
@Composable
fun BikeOSNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = BikeOSDestinations.DASHBOARD) {
        composable(BikeOSDestinations.DASHBOARD) { DashboardScreen(navController) }
        composable(BikeOSDestinations.MENU_HOME) { HomeScreen(navController) }
        composable(BikeOSDestinations.MENU_APPEARANCE) { AppearanceScreen() }
        composable(BikeOSDestinations.MENU_CALCULATOR) { CalculatorScreen() }
        composable(BikeOSDestinations.MENU_SETTINGS) { SettingsScreen() }
        composable(BikeOSDestinations.MENU_ACCOUNT) { AccountScreen() }
        composable(BikeOSDestinations.MENU_ABOUT) { AboutScreen() }
    }
}
