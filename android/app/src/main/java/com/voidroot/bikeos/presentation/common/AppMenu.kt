package com.voidroot.bikeos.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeSurface
import com.voidroot.bikeos.core.theme.BikeTextPrimary

private data class MenuEntry(val label: String, val route: String, val icon: ImageVector)

private val menuEntries = listOf(
    MenuEntry("Home", BikeOSDestinations.MENU_HOME, Icons.Filled.Home),
    MenuEntry("Calculator", BikeOSDestinations.MENU_CALCULATOR, Icons.Filled.Calculate),
    MenuEntry("Settings", BikeOSDestinations.MENU_SETTINGS, Icons.Filled.Settings),
    MenuEntry("About", BikeOSDestinations.MENU_ABOUT, Icons.Filled.Info),
    MenuEntry("Profile", BikeOSDestinations.MENU_ACCOUNT, Icons.Filled.AccountCircle)
)

/**
 * The hamburger menu - shown on every top-level screen (Home, Calculator,
 * Settings, About, Profile) so switching between them never requires
 * backing out to Home first. Exactly the 5 destinations per the product
 * spec - Appearance is reached from inside Settings, not this menu.
 *
 * Uses the bottom-nav-style back stack pattern (popUpTo the graph start +
 * saveState/restoreState) so the back stack doesn't grow every time the
 * menu is used - pressing system back from any menu screen goes to Home,
 * not through a long chain of every menu screen visited in order.
 */
@Composable
fun AppMenuButton(navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BikeTextPrimary)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(BikeSurface, RoundedCornerShape(16.dp))
    ) {
        menuEntries.forEach { entry ->
            DropdownMenuItem(
                text = { Text(entry.label, style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(entry.icon, contentDescription = null, tint = BikeAccent) },
                onClick = {
                    expanded = false
                    navController.navigate(entry.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
