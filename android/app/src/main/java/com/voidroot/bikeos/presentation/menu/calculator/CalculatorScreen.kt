package com.voidroot.bikeos.presentation.menu.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.MenuScreenHeader

/** Placeholder body - gear-ratio / speed / calorie calculator logic lands in its own dedicated phase. */
@Composable
fun CalculatorScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MenuScreenHeader("Calculator", navController)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Gear ratio, speed/distance/time, and calorie calculators are coming here.",
                style = MaterialTheme.typography.bodyMedium,
                color = BikeTextSecondary
            )
        }
    }
}
