package com.voidroot.bikeos.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.BikeTextSecondary

data class PermissionRationale(val permissionLabel: String, val reason: String)

/**
 * Shown when the user denies a permission request - lists exactly what's
 * being asked for and why, per the trust-building ask: don't just silently
 * fail or re-prompt, explain first. Reused across every feature that needs
 * a sensitive permission (contacts for the Calls widget, notification
 * access for the Music widget, etc.) rather than each screen rolling its
 * own explanation dialog.
 */
@Composable
fun PermissionRationaleDialog(
    items: List<PermissionRationale>,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission needed") },
        text = {
            Column {
                items.forEach { item ->
                    Text(item.permissionLabel, modifier = androidx.compose.ui.Modifier.padding(top = 8.dp))
                    Text(item.reason, color = BikeTextSecondary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        }
    )
}
