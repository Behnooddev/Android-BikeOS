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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikeSuccess
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.data.ble.BleConnectionState

/**
 * Settings: Theme, Units, Alerts, Bluetooth, Bike Configuration, Backup,
 * and Erase Data - per the product spec's Settings section. Per-widget
 * enable/disable and day/night cluster colors live on the Appearance
 * screen (reached from here, not from the main hamburger menu).
 *
 * Bike config fields write straight through to Room on every change rather
 * than batching into a local draft + Save button (unlike AccountScreen) -
 * simpler, at the cost of a DB write per keystroke. Debouncing this is a
 * reasonable follow-up polish item, not a correctness issue.
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)

        SectionTitle("Theme")
        SettingsToggleRow("Dark theme (off = light)", uiState.settings.isDarkTheme, viewModel::setDarkTheme)
        SettingsToggleRow("24-hour clock (off = 12-hour)", uiState.settings.use24HourClock, viewModel::setUse24HourClock)
        SettingsToggleRow(
            "Engine-start animation when entering the cluster",
            uiState.settings.engineStartAnimationEnabled,
            viewModel::setEngineStartAnimationEnabled
        )
        Button(onClick = { navController.navigate(BikeOSDestinations.MENU_APPEARANCE) }) {
            Text("Appearance - widgets & cluster colors")
        }

        HorizontalDivider()
        SectionTitle("Units")
        SettingsToggleRow("Use kilometers (off = miles)", uiState.settings.useMetricUnits, viewModel::setMetricUnits)

        SectionTitle("Alerts & Suggestions")
        SettingsToggleRow("Sound", uiState.settings.soundEnabled, viewModel::setSoundEnabled)
        SettingsToggleRow("Gear suggestions", uiState.settings.gearSuggestionsEnabled, viewModel::setGearSuggestionsEnabled)
        SettingsToggleRow("Reminder notifications", uiState.settings.reminderNotificationsEnabled, viewModel::setReminderNotificationsEnabled)
        SettingsToggleRow("Anti-theft alarm", uiState.settings.antiTheftAlarmEnabled, viewModel::setAntiTheftAlarmEnabled)
        OutlinedTextField(
            value = uiState.settings.maxSpeedAlertKmh.toString(),
            onValueChange = { v -> v.toIntOrNull()?.let(viewModel::setMaxSpeedAlert) },
            label = { Text("Max speed alert (km/h)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        SectionTitle("Bluetooth Configuration")
        val (statusText, statusColor) = when (val state = connectionState) {
            is BleConnectionState.Disconnected -> "Disconnected" to BikeTextSecondary
            is BleConnectionState.Scanning -> "Scanning..." to BikeAccent
            is BleConnectionState.Connecting -> "Connecting..." to BikeAccent
            is BleConnectionState.Connected -> "Connected" to BikeSuccess
            is BleConnectionState.Failed -> "Failed: ${state.reason}" to BikeDanger
        }
        Text(statusText, color = statusColor)
        deviceInfo?.let { info ->
            Text(
                "Device: ${info.deviceId}" + (info.firmwareVersion?.let { " · fw $it" } ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = BikeTextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { permissionLauncher.launch(bluetoothPermissions) }) {
                Text("Scan & Connect")
            }
            OutlinedButton(onClick = viewModel::disconnectDevice) {
                Text("Disconnect")
            }
        }

        HorizontalDivider()
        SectionTitle("Bike Configuration")
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

        HorizontalDivider()
        SectionTitle("Backup (.bop)")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = viewModel::exportBackup) { Text("Export") }
            OutlinedButton(onClick = viewModel::importBackup) { Text("Import") }
        }
        uiState.backupMessage?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
        }

        HorizontalDivider()
        SectionTitle("Danger Zone")
        Button(onClick = { showEraseConfirm = true }) {
            Text("Erase all data")
        }
        Text(
            "Deletes your profile, bike config, ride history, and settings, and takes you back through onboarding.",
            style = MaterialTheme.typography.labelSmall,
            color = BikeTextSecondary
        )
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

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = BikeTextSecondary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = BikeTextPrimary)
}
