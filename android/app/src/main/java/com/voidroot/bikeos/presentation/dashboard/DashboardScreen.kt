package com.voidroot.bikeos.presentation.dashboard

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.common.ImmersiveMode
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
 * Root cockpit screen - fully immersive (system bars hidden, see
 * [ImmersiveMode]) and themed via [LocalClusterPalette] (user-customizable
 * day/night colors, resolved in [DashboardViewModel]).
 *
 * Sensor values come from [DashboardViewModel]'s SensorRepository (real
 * BLE data when connected, honest zeros otherwise - never fake). Gear
 * comes from the Room-backed bike profile, bottom-row cards are
 * individually toggleable via the Appearance screen, Start/Stop Ride
 * persists a completed RideSession to Room, and the Bike Control Panel's
 * light toggles send real Control Service commands to the ESP32. Mode/Gear
 * -and, while a call is ringing, Answer/Reject- can also be driven from
 * the physical handlebar buttons.
 *
 * Calls and Music widgets each need their own permission: Calls needs
 * READ_PHONE_STATE/READ_CONTACTS/ANSWER_PHONE_CALLS (normal runtime
 * prompt, requested here on first entry); Music needs "Notification
 * access" (a special permission only grantable from system settings - see
 * MusicRepository's kdoc), so instead of a prompt this screen shows a
 * button that deep-links there, only when the Music widget is enabled and
 * access isn't granted yet.
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(clusterPalette.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopStatusRow(
                isConnected = uiState.isConnected,
                batteryPercent = uiState.batteryPercent,
                currentTime = uiState.currentTime,
                isRideActive = uiState.isRideActive,
                onToggleRide = {
                    if (uiState.isRideActive) viewModel.stopRide() else viewModel.startRide()
                },
                onBack = { navController.popBackStack() }
            )

            if (WidgetKeys.CALLS in uiState.enabledWidgetKeys) {
                CallWidget(incomingCall = incomingCall, modifier = Modifier.padding(top = 8.dp))
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpeedGauge(speedKmh = uiState.speedKmh, maxSpeedKmh = uiState.maxSpeedKmh)
                    RideModeSelector(
                        selected = uiState.rideMode,
                        onSelect = viewModel::onRideModeSelected,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    LightControlRow(
                        lightState = lightState,
                        onToggleFront = viewModel::toggleFrontLight,
                        onToggleRear = viewModel::toggleRearLight,
                        onToggleBody = viewModel::toggleBodyLight,
                        modifier = Modifier.padding(top = 10.dp)
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

@Composable
private fun TopStatusRow(
    isConnected: Boolean,
    batteryPercent: Int,
    currentTime: String,
    isRideActive: Boolean,
    onToggleRide: () -> Unit,
    onBack: () -> Unit
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
        GlassCard(modifier = Modifier.clickable(onClick = onToggleRide)) {
            Text(
                text = if (isRideActive) "■ Stop Ride" else "▶ Start Ride",
                style = MaterialTheme.typography.labelSmall,
                color = if (isRideActive) BikeDanger else palette.accent
            )
        }
        // Top-right (landscape) - exits the cluster back to Home. Deliberately
        // NOT a system-back-only affordance: riders with gloves/mounted
        // phones need a visible tap target, not a gesture/hardware button.
        GlassCard(modifier = Modifier.clickable(onClick = onBack)) {
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
