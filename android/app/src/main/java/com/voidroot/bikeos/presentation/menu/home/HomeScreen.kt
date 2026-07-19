package com.voidroot.bikeos.presentation.menu.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.AppMenuButton

/**
 * The permanent landing screen from the second app open onward (Splash
 * routes here once onboarding+signup are done). The Start button is the
 * ONLY way into the cluster/Dashboard - everything else here is
 * informational or navigation.
 */
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(BikeBackground)) {
        AppMenuButton(navController) // top-left, per spec

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.greetingMessage,
                style = MaterialTheme.typography.headlineMedium,
                color = BikeTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 56.dp, start = 40.dp, end = 40.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Brush.linearGradient(listOf(BikePrimary, BikeAccent)), CircleShape)
                        .clickable { navController.navigate(BikeOSDestinations.CLUSTER_BOOT) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("START", style = MaterialTheme.typography.titleMedium, color = BikeBackground)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Total Distance", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
                        Text(
                            String.format("%.1f km", uiState.totalDistanceKm),
                            style = MaterialTheme.typography.titleMedium,
                            color = BikeTextPrimary
                        )
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Riding Style", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
                        Text(
                            uiState.ridingStyleSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BikeTextPrimary
                        )
                    }
                }
            }
        }
    }
}
