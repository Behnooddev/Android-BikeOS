package com.voidroot.bikeos.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.theme.BikeTextPrimary

/**
 * Consistent header (menu button + title) for every top-level menu screen
 * (Settings, Calculator, About, Profile) - having the menu button on every
 * screen, not just Home, is what lets the rider switch between them
 * directly instead of backing out to Home first every time.
 */
@Composable
fun MenuScreenHeader(title: String, navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppMenuButton(navController)
        Text(title, style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)
        Spacer(modifier = Modifier.width(48.dp)) // balances the menu icon so the title visually centers
    }
}
