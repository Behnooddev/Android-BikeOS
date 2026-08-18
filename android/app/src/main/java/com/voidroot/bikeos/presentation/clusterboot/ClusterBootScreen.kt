package com.voidroot.bikeos.presentation.clusterboot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.ImmersiveMode
import com.voidroot.bikeos.core.common.LandscapeOnly
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.presentation.common.PermissionRationale
import com.voidroot.bikeos.presentation.common.PermissionRationaleDialog
import kotlinx.coroutines.delay

private enum class BootPhase { CONNECTING, ANIMATING }
private const val CONNECT_TIMEOUT_MS = 8000L

/**
 * Reached when Start is pressed on Home. Attempts a BLE connection, then
 * (once connected, or once the user gives up waiting) plays the
 * engine-start animation before landing on the real Dashboard - see
 * EngineStartAnimation.kt / ConnectingUi.kt for the two phases.
 */
@Composable
fun ClusterBootScreen(navController: NavHostController, viewModel: ClusterBootViewModel = hiltViewModel()) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val animationEnabled by viewModel.engineAnimationEnabled.collectAsStateWithLifecycle()
    val hardwareFreeMode by viewModel.hardwareFreeModeEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Hardware-free mode never waits on a BLE connection, so it starts
    // straight in ANIMATING - there's nothing to "connect" to at boot.
    var bootPhase by remember(hardwareFreeMode) {
        mutableStateOf(if (hardwareFreeMode) BootPhase.ANIMATING else BootPhase.CONNECTING)
    }
    var showTimeoutFallback by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    ImmersiveMode()
    LandscapeOnly()

    val bluetoothPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) viewModel.connect() else showPermissionRationale = true
    }

    // Hardware-free mode: ask for location once, optimistically, but never
    // block boot on the result - per product decision, the dashboard opens
    // with speed/distance dashed until the user grants it (see
    // PhoneSensorSource.noPermissionStream). Denying just means Settings
    // is where they'll need to go to turn it on later.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result intentionally unused - PhoneSensorSource re-checks live */ }

    fun goToDashboard() {
        navController.navigate(BikeOSDestinations.DASHBOARD) {
            popUpTo(BikeOSDestinations.CLUSTER_BOOT) { inclusive = true }
        }
    }

    // Kick off connection attempt once, on entering this screen - skipped
    // entirely in hardware-free mode.
    LaunchedEffect(hardwareFreeMode) {
        if (hardwareFreeMode) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyGranted) locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            val alreadyGranted = bluetoothPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            if (alreadyGranted) viewModel.connect() else bluetoothPermissionLauncher.launch(bluetoothPermissions)
        }
    }

    // Timeout: stop waiting for a connection after CONNECT_TIMEOUT_MS.
    // N/A in hardware-free mode, which never enters CONNECTING at all.
    LaunchedEffect(hardwareFreeMode) {
        if (!hardwareFreeMode) {
            delay(CONNECT_TIMEOUT_MS)
            if (connectionState !is BleConnectionState.Connected) showTimeoutFallback = true
        }
    }

    // Once actually connected, move straight into the boot animation phase.
    LaunchedEffect(connectionState) {
        if (connectionState is BleConnectionState.Connected && bootPhase == BootPhase.CONNECTING) {
            bootPhase = BootPhase.ANIMATING
        }
    }

    when (bootPhase) {
        BootPhase.CONNECTING -> ConnectingUi(
            showContinueAnyway = showTimeoutFallback,
            onContinueAnyway = { bootPhase = BootPhase.ANIMATING }
        )
        BootPhase.ANIMATING -> EngineStartAnimation(
            animationEnabled = animationEnabled,
            onFinished = { goToDashboard() }
        )
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            items = listOf(
                PermissionRationale(
                    "Bluetooth",
                    "Needed to connect to your BikeOS controller for live speed, cadence, and battery data."
                )
            ),
            onDismiss = {
                showPermissionRationale = false
                showTimeoutFallback = true // no point waiting out the timeout if permission was denied
            },
            onOpenSettings = {
                showPermissionRationale = false
                showTimeoutFallback = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}
