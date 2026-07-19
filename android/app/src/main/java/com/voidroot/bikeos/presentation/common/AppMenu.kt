package com.voidroot.bikeos.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeTextPrimary

/**
 * The hamburger menu shown top-left on Home (and reusable anywhere else
 * that needs it). Exactly 5 destinations per the product spec: Home,
 * Calculator, Settings, About, Profile - Appearance is reached from inside
 * Settings instead of being a top-level menu item.
 */
@Composable
fun AppMenuButton(navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BikeTextPrimary)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Home") }, onClick = {
            expanded = false
            navController.navigate(BikeOSDestinations.MENU_HOME) { launchSingleTop = true }
        })
        DropdownMenuItem(text = { Text("Calculator") }, onClick = {
            expanded = false
            navController.navigate(BikeOSDestinations.MENU_CALCULATOR) { launchSingleTop = true }
        })
        DropdownMenuItem(text = { Text("Settings") }, onClick = {
            expanded = false
            navController.navigate(BikeOSDestinations.MENU_SETTINGS) { launchSingleTop = true }
        })
        DropdownMenuItem(text = { Text("About") }, onClick = {
            expanded = false
            navController.navigate(BikeOSDestinations.MENU_ABOUT) { launchSingleTop = true }
        })
        DropdownMenuItem(text = { Text("Profile") }, onClick = {
            expanded = false
            navController.navigate(BikeOSDestinations.MENU_ACCOUNT) { launchSingleTop = true }
        })
    }
}
