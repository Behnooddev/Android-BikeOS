package com.voidroot.bikeos.presentation.menu.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikeSuccess
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.presentation.common.BikeOSMenuScaffold

/**
 * Settings, redesigned as grouped cards (one GlassCard per topic, each
 * with an icon + title) rather than a flat list with plain dividers -
 * per the "professional, not messy" feedback. Per-widget enable/disable
 * and day/night cluster colors live on the Appearance screen (reached
 * from here, not from the main hamburger menu).
 */
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()
    val bike = uiState.bike
    var showEraseConfirm by remember { mutableStateOf(false) }

    val bluetoothPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) viewModel.connectDevice()
    }

    BikeOSMenuScaffold(navController, "Settings") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSection(title = "Theme & Display", icon = Icons.Filled.Palette) {
            SettingsToggleRow("Dark theme", "Off switches to a light appearance", uiState.settings.isDarkTheme, viewModel::setDarkTheme)
            SettingsToggleRow("24-hour clock", "Off shows 12-hour time", uiState.settings.use24HourClock, viewModel::setUse24HourClock)
            SettingsToggleRow(
                "Engine-start animation",
                "The gauge sweep + sound when entering the cluster",
                uiState.settings.engineStartAnimationEnabled,
                viewModel::setEngineStartAnimationEnabled
            )
            OutlinedButton(
                onClick = { navController.navigate(BikeOSDestinations.MENU_APPEARANCE) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Appearance - widgets & cluster colors") }
        }

        SettingsSection(title = "Units & Alerts", icon = Icons.Filled.Speed) {
            SettingsToggleRow("Kilometers", "Off switches to miles", uiState.settings.useMetricUnits, viewModel::setMetricUnits)
            SettingsToggleRow("Sound", "Alert and notification sounds", uiState.settings.soundEnabled, viewModel::setSoundEnabled)
            SettingsToggleRow("Gear suggestions", "Recommendations while riding", uiState.settings.gearSuggestionsEnabled, viewModel::setGearSuggestionsEnabled)
            OutlinedTextField(
                value = uiState.settings.maxSpeedAlertKmh.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let(viewModel::setMaxSpeedAlert) },
                label = { Text("Max speed alert (km/h)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsSection(title = "Reminders & Security", icon = Icons.Filled.Security) {
            SettingsToggleRow("Ride reminders", "A nudge around your usual ride time if you haven't ridden today", uiState.settings.reminderNotificationsEnabled, viewModel::setReminderNotificationsEnabled)
            SettingsToggleRow("Anti-theft alarm", "Buzzer + blinking lights if the bike is disturbed while armed", uiState.settings.antiTheftAlarmEnabled, viewModel::setAntiTheftAlarmEnabled)
        }

        SettingsSection(title = "Bluetooth Configuration", icon = Icons.Filled.Bluetooth) {
            val (statusText, statusColor) = when (val state = connectionState) {
                is BleConnectionState.Disconnected -> "Disconnected" to BikeTextSecondary
                is BleConnectionState.Scanning -> "Scanning..." to BikeAccent
                is BleConnectionState.Connecting -> "Connecting..." to BikeAccent
                is BleConnectionState.Connected -> "Connected" to BikeSuccess
                is BleConnectionState.Failed -> "Failed: ${state.reason}" to BikeDanger
            }
            Text(statusText, color = statusColor, style = MaterialTheme.typography.bodyMedium)
            deviceInfo?.let { info ->
                Text(
                    "Device: ${info.deviceId}" + (info.firmwareVersion?.let { " · fw $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = BikeTextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { permissionLauncher.launch(bluetoothPermissions) }) { Text("Scan & Connect") }
                OutlinedButton(onClick = viewModel::disconnectDevice) { Text("Disconnect") }
            }
        }

        SettingsSection(title = "Bike Configuration", icon = Icons.Filled.DirectionsBike) {
            OutlinedTextField(
                value = bike.bikeName,
                onValueChange = { viewModel.saveBikeConfig(bike.copy(bikeName = it)) },
                label = { Text("Bike name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bike.bikeType,
                onValueChange = { viewModel.saveBikeConfig(bike.copy(bikeType = it)) },
                label = { Text("Bike type") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bike.wheelSizeInches.toString(),
                onValueChange = { v -> v.toFloatOrNull()?.let { viewModel.saveBikeConfig(bike.copy(wheelSizeInches = it)) } },
                label = { Text("Wheel size (inches)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bike.frontGearCount.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { viewModel.saveBikeConfig(bike.copy(frontGearCount = it)) } },
                    label = { Text("Front gears") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = bike.rearGearCount.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { viewModel.saveBikeConfig(bike.copy(rearGearCount = it)) } },
                    label = { Text("Rear gears") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
            Text(
                "Total combinations: ${bike.totalGearCombinations}",
                style = MaterialTheme.typography.labelSmall,
                color = BikeTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bike.currentFrontGear.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { viewModel.syncGear(it, bike.currentRearGear) } },
                    label = { Text("Current front gear") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                OutlinedTextField(
                    value = bike.currentRearGear.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { viewModel.syncGear(bike.currentFrontGear, it) } },
                    label = { Text("Current rear gear") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }

        SettingsSection(title = "Backup (.bop)", icon = Icons.Filled.Save) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::exportBackup) { Text("Export") }
                OutlinedButton(onClick = viewModel::importBackup) { Text("Import") }
            }
            uiState.backupMessage?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
            }
        }

        SettingsSection(title = "Danger Zone", icon = Icons.Filled.WarningAmber, accentColor = BikeDanger) {
            Text(
                "Deletes your profile, bike config, ride history, colors, and settings, then takes you back through onboarding.",
                style = MaterialTheme.typography.labelSmall,
                color = BikeTextSecondary
            )
            Button(
                onClick = { showEraseConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = BikeDanger),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Erase all data") }
        }
    }

    if (showEraseConfirm) {
        AlertDialog(
            onDismissRequest = { showEraseConfirm = false },
            title = { Text("Erase all data?") },
            text = { Text("This cannot be undone. Consider exporting a .bop backup first.") },
            confirmButton = {
                TextButton(onClick = {
                    showEraseConfirm = false
                    viewModel.eraseAllData {
                        navController.navigate(BikeOSDestinations.SPLASH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("Erase", color = BikeDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showEraseConfirm = false }) { Text("Cancel") }
            }
        )
    }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color = BikeAccent,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.padding(end = 8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = BikeTextPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = BikeTextPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = BikeTextSecondary, style = MaterialTheme.typography.labelSmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
