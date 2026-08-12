package com.voidroot.bikeos.presentation.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.common.ImmersiveMode
import com.voidroot.bikeos.core.common.LandscapeOnly
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikeSuccess
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import com.voidroot.bikeos.data.repository.WidgetKeys
import com.voidroot.bikeos.presentation.dashboard.components.CallWidget
import com.voidroot.bikeos.presentation.dashboard.components.LightControlRow
import com.voidroot.bikeos.presentation.dashboard.components.MusicWidget
import com.voidroot.bikeos.presentation.dashboard.components.RideModeSelector
import com.voidroot.bikeos.presentation.dashboard.components.SpeedGauge

/**
 * Root cockpit screen - landscape-only (see [LandscapeOnly]) and fully
 * immersive (system bars hidden, see [ImmersiveMode]), themed via
 * [LocalClusterPalette] (user-customizable day/night colors, resolved in
 * [DashboardViewModel]).
 *
 * Ride tracking is automatic: entering this screen starts it, and Exit
 * (or the system back gesture/button - both routed through the same
 * [DashboardViewModel.exitCluster]) stops and saves it. There is no
 * separate manual Start/Stop Ride control - entering the cockpit IS
 * starting the ride, which is a simpler mental model than a second
 * "start" button living inside the thing Home's Start button already
 * took you into.
 *
 * Sensor values come from [DashboardViewModel]'s SensorRepository (real
 * BLE data when connected, honest zeros otherwise - never fake). Gear
 * comes from the Room-backed bike profile, bottom-row cards are
 * individually toggleable via the Appearance screen, and the Bike Control
 * Panel's light toggles send real Control Service commands to the ESP32.
 * Mode/Gear -and, while a call is ringing, Answer/Reject- can also be
 * driven from the physical handlebar buttons.
 */
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lightState by viewModel.lightState.collectAsStateWithLifecycle()
    val clusterPalette by viewModel.clusterPalette.collectAsStateWithLifecycle()
    val incomingCall by viewModel.incomingCall.collectAsStateWithLifecycle()
    val musicState by viewModel.musicState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ImmersiveMode()
    LandscapeOnly()

    fun exit() {
        viewModel.exitCluster { navController.popBackStack() }
    }

    // System back (gesture or hardware button) exits the same way the
    // Exit button does, so a ride is always saved regardless of how the
    // rider leaves the cluster.
    BackHandler(onBack = ::exit)

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) viewModel.startCallListening()
    }

    LaunchedEffect(Unit) {
        if (WidgetKeys.CALLS in uiState.enabledWidgetKeys && !viewModel.hasCallPermissions()) {
            callPermissionLauncher.launch(viewModel.callPermissions())
        }
    }

    CompositionLocalProvider(LocalClusterPalette provides clusterPalette) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(clusterPalette.background, clusterPalette.background.copy(alpha = 0.92f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                TopStatusRow(
                    isConnected = uiState.isConnected,
                    batteryPercent = uiState.batteryPercent,
                    currentTime = uiState.currentTime,
                    onExit = ::exit
                )

                if (WidgetKeys.CALLS in uiState.enabledWidgetKeys) {
                    CallWidget(incomingCall = incomingCall, modifier = Modifier.padding(top = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpeedGauge(speedKmh = uiState.speedKmh, maxSpeedKmh = uiState.maxSpeedKmh)
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 24.dp).widthIn(max = 280.dp)
                    ) {
                        RideModeSelector(
                            selected = uiState.rideMode,
                            onSelect = viewModel::onRideModeSelected
                        )
                        LightControlRow(
                            lightState = lightState,
                            onToggleFront = viewModel::toggleFrontLight,
                            onToggleRear = viewModel::toggleRearLight,
                            onToggleBody = viewModel::toggleBodyLight,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                if (WidgetKeys.MUSIC in uiState.enabledWidgetKeys) {
                    if (viewModel.hasNotificationAccess()) {
                        MusicWidget(
                            state = musicState,
                            onPlayPause = viewModel::musicPlayPause,
                            onNext = viewModel::musicNext,
                            onPrevious = viewModel::musicPrevious,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    } else {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }
                        ) {
                            Text(
                                "Enable Notification access to control music from here",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalClusterPalette.current.textSecondary
                            )
                        }
                    }
                }

                BottomInfoRow(
                    enabledWidgetKeys = uiState.enabledWidgetKeys,
                    distanceKm = uiState.distanceKm,
                    calories = uiState.calories,
                    cadenceRpm = uiState.cadenceRpm,
                    frontGear = uiState.frontGear,
                    rearGear = uiState.rearGear
                )
            }
        }
    }
}

@Composable
private fun TopStatusRow(
    isConnected: Boolean,
    batteryPercent: Int,
    currentTime: String,
    onExit: () -> Unit
) {
    val palette = LocalClusterPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isConnected) BikeSuccess else BikeDanger, CircleShape)
                )
                Text(
                    text = if (isConnected) "  Connected" else "  Disconnected",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
            }
        }
        GlassCard { Text("$batteryPercent%", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary) }
        GlassCard { Text(currentTime, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary) }
        // Top-right (landscape) - exits the cluster, saving the ride.
        // Riders with gloves/mounted phones need a visible tap target,
        // not just a gesture/hardware button (though that works too - see BackHandler).
        GlassCard(modifier = Modifier.clickable(onClick = onExit)) {
            Text("✕ Exit", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
        }
    }
}

@Composable
private fun BottomInfoRow(
    enabledWidgetKeys: Set<String>,
    distanceKm: Float,
    calories: Int,
    cadenceRpm: Int,
    frontGear: Int,
    rearGear: Int
) {
    val palette = LocalClusterPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (WidgetKeys.DISTANCE in enabledWidgetKeys) {
            GlassCard {
                Column {
                    Text("Distance", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                    Text(
                        String.format("%.1f km", distanceKm),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                }
            }
        }
        if (WidgetKeys.CALORIES in enabledWidgetKeys) {
            GlassCard {
                Column {
                    Text("Calories", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                    Text("$calories kcal", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
            }
        }
        if (WidgetKeys.CADENCE in enabledWidgetKeys) {
            GlassCard {
                Column {
                    Text("Cadence", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                    Text("$cadenceRpm rpm", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
            }
        }
        if (WidgetKeys.GEAR in enabledWidgetKeys) {
            GlassCard {
                Column {
                    Text("Gear", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                    Text("$frontGear x $rearGear", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                }
            }
        }
    }
}
