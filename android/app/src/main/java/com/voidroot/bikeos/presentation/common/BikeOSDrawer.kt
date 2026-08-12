package com.voidroot.bikeos.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeSurface
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

private data class DrawerEntry(val label: String, val route: String, val icon: ImageVector)

private val drawerEntries = listOf(
    DrawerEntry("Home", BikeOSDestinations.MENU_HOME, Icons.Filled.Home),
    DrawerEntry("Calculator", BikeOSDestinations.MENU_CALCULATOR, Icons.Filled.Calculate),
    DrawerEntry("Settings", BikeOSDestinations.MENU_SETTINGS, Icons.Filled.Settings),
    DrawerEntry("About", BikeOSDestinations.MENU_ABOUT, Icons.Filled.Info),
    DrawerEntry("Profile", BikeOSDestinations.MENU_ACCOUNT, Icons.Filled.AccountCircle)
)

/**
 * The full-height slide-in navigation drawer - replaces the earlier small
 * dropdown menu per the "menu should be a real full-screen-height drawer,
 * not a tiny box" feedback. [ModalDrawerSheet] already spans the full
 * device height by Material Design spec, so this is mostly styling: a
 * branded header (gradient + app name), then the 5 nav destinations with
 * icons and a highlighted current-route state, with a version footer
 * pinned to the bottom.
 */
@Composable
fun BikeOSDrawerContent(navController: NavHostController, onItemSelected: () -> Unit) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight().width(280.dp),
        drawerContainerColor = BikeSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.linearGradient(listOf(BikePrimary, BikeAccent))),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(BikeBackground.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("B", color = BikeBackground, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Text(
                    "BikeOS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BikeBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        drawerEntries.forEach { entry ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == entry.route } == true
            NavigationDrawerItem(
                label = {
                    Text(
                        entry.label,
                        color = if (isSelected) BikeTextPrimary else BikeTextSecondary
                    )
                },
                icon = {
                    Icon(entry.icon, contentDescription = null, tint = if (isSelected) BikeAccent else BikeTextSecondary)
                },
                selected = isSelected,
                onClick = {
                    onItemSelected()
                    navController.navigate(entry.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = BikeAccent.copy(alpha = 0.15f),
                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
        Row(modifier = Modifier.padding(20.dp)) {
            Text("BikeOS - VoidRoot", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
        }
    }
}
