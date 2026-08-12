package com.voidroot.bikeos.presentation.menu.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.BikeOSMenuScaffold

/** App/developer info - version, brand, license. */
@Composable
fun AboutScreen(navController: NavHostController) {
    BikeOSMenuScaffold(navController, "About") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("BikeOS", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = BikeAccent)
                    Text("Smart bicycle cockpit platform", style = MaterialTheme.typography.bodyMedium, color = BikeTextSecondary)
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("Developer", "Behnood Shafiei")
                    InfoRow("Brand", "VoidRoot")
                    InfoRow("License", "Proprietary")
                    InfoRow("Version", "See build info in your installed package")
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "BikeOS turns your phone into a digital bicycle cockpit, paired over Bluetooth with a BikeOS ESP32 controller for live sensor data, lights, and an anti-theft alarm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BikeTextSecondary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = BikeTextPrimary)
    }
}
