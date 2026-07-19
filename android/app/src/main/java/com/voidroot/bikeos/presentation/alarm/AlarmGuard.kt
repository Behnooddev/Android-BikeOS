package com.voidroot.bikeos.presentation.alarm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voidroot.bikeos.core.theme.BikeDanger

/**
 * Wraps the whole app (see MainActivity). Shows a quick-disarm password
 * dialog on top of WHATEVER screen is visible the moment the ESP32 reports
 * the anti-theft alarm triggered - deliberately not tied to any one route,
 * since the point is speed, not "go find the right screen first at 2am".
 */
@Composable
fun AlarmGuard(viewModel: AlarmGuardViewModel = hiltViewModel(), content: @Composable () -> Unit) {
    content()

    val isTriggered by viewModel.isTriggered.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    if (isTriggered) {
        var password by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { /* not dismissible without the password - that's the point */ },
            title = { Text("Bike alarm triggered") },
            text = {
                Column {
                    Text("Your bike's alarm has gone off. Enter your password to disarm it.")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMessage?.let { Text(it, color = BikeDanger) }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.attemptDisarm(password) }) { Text("Disarm") }
            }
        )
    }
}
