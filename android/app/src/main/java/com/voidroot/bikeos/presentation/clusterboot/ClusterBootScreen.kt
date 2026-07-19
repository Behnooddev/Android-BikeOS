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
    val context = LocalContext.current

    var bootPhase by remember { mutableStateOf(BootPhase.CONNECTING) }
    var showTimeoutFallback by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    ImmersiveMode()

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
        if (results.values.all { it }) viewModel.connect() else showPermissionRationale = true
    }

    fun goToDashboard() {
        navController.navigate(BikeOSDestinations.DASHBOARD) {
            popUpTo(BikeOSDestinations.CLUSTER_BOOT) { inclusive = true }
        }
    }

    // Kick off connection attempt once, on entering this screen.
    LaunchedEffect(Unit) {
        val alreadyGranted = bluetoothPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) viewModel.connect() else permissionLauncher.launch(bluetoothPermissions)
    }

    // Timeout: stop waiting for a connection after CONNECT_TIMEOUT_MS.
    LaunchedEffect(Unit) {
        delay(CONNECT_TIMEOUT_MS)
        if (connectionState !is BleConnectionState.Connected) showTimeoutFallback = true
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
