package com.voidroot.bikeos.presentation.menu.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ride history list - reads completed [com.voidroot.bikeos.data.repository.RideSession]
 * rows written by the Dashboard's Start/Stop Ride control.
 */
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val rides by viewModel.recentRides.collectAsStateWithLifecycle()
    val dateFormat = remember(Unit) { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Ride History", style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)

        if (rides.isEmpty()) {
            Text(
                "No rides yet - use Start Ride on the dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = BikeTextSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rides) { ride ->
                    GlassCard {
                        Column {
                            Text(
                                dateFormat.format(Date(ride.startTimeEpochMs)),
                                style = MaterialTheme.typography.titleMedium,
                                color = BikeTextPrimary
                            )
                            Text(
                                "${String.format("%.1f", ride.distanceKm)} km · " +
                                    "${ride.durationSeconds / 60} min · ${ride.calories} kcal · " +
                                    "avg ${String.format("%.0f", ride.avgSpeedKmh)} km/h",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BikeTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
