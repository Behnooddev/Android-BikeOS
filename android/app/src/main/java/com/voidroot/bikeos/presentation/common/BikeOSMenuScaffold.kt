package com.voidroot.bikeos.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import kotlinx.coroutines.launch

/**
 * Shared scaffold for every top-level menu screen (Home, Calculator,
 * Settings, About, Profile) - wraps [BikeOSDrawerContent] (a real
 * full-height slide-in drawer, per the "menu should reach the bottom of
 * the screen, not be a small dropdown" feedback) around the screen's own
 * content, with a small top row (menu button + title) to open it.
 *
 * Replaces the earlier `MenuScreenHeader` + `AppMenuButton` dropdown
 * combo - kept as one shared composable so every screen behaves
 * identically rather than five separate hand-rolled headers.
 */
@Composable
fun BikeOSMenuScaffold(
    navController: NavHostController,
    title: String,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BikeOSDrawerContent(navController) { scope.launch { drawerState.close() } }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().background(BikeBackground)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BikeTextPrimary)
                }
                Text(title, style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
    }
}
