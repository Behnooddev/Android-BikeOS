package com.voidroot.bikeos.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

/**
 * First-run account creation + bike setup, combined into one flow (per
 * the ask: bike configuration happens right at signup, not a separate
 * trip to Settings afterward). Only shown once (see
 * AppState.hasCompletedSignup) - from the second app open onward, HOME is
 * the landing screen.
 */
@Composable
fun SignupScreen(navController: NavHostController, viewModel: SignupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BikeBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Create your BikeOS account", style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)
        Text(
            "Your password also works as the anti-theft alarm's quick-disarm code.",
            style = MaterialTheme.typography.labelSmall,
            color = BikeTextSecondary
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Account")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = { v -> viewModel.update { it.copy(firstName = v) } },
                        label = { Text("First name") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = { v -> viewModel.update { it.copy(lastName = v) } },
                        label = { Text("Last name") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { v -> viewModel.update { it.copy(username = v) } },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { v -> viewModel.update { it.copy(email = v) } },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { v -> viewModel.update { it.copy(password = v) } },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { v -> viewModel.update { it.copy(confirmPassword = v) } },
                    label = { Text("Confirm password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Physical Stats")
                Text(
                    "Used to personalize calorie estimates.",
                    style = MaterialTheme.typography.labelSmall,
                    color = BikeTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.age,
                        onValueChange = { v -> viewModel.update { it.copy(age = v) } },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = state.heightCm,
                        onValueChange = { v -> viewModel.update { it.copy(heightCm = v) } },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = state.weightKg,
                        onValueChange = { v -> viewModel.update { it.copy(weightKg = v) } },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Bike Configuration")
                OutlinedTextField(
                    value = state.bikeName,
                    onValueChange = { v -> viewModel.update { it.copy(bikeName = v) } },
                    label = { Text("Bike name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.bikeType,
                    onValueChange = { v -> viewModel.update { it.copy(bikeType = v) } },
                    label = { Text("Bike type") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.wheelSizeInches,
                    onValueChange = { v -> viewModel.update { it.copy(wheelSizeInches = v) } },
                    label = { Text("Wheel size (inches)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.frontGearCount,
                        onValueChange = { v -> viewModel.update { it.copy(frontGearCount = v) } },
                        label = { Text("Front gears") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = state.rearGearCount,
                        onValueChange = { v -> viewModel.update { it.copy(rearGearCount = v) } },
                        label = { Text("Rear gears") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
            }
        }

        state.errorMessage?.let {
            Text(it, color = BikeDanger, style = MaterialTheme.typography.labelSmall)
        }

        Button(
            onClick = {
                viewModel.submit {
                    navController.navigate(BikeOSDestinations.MENU_HOME) {
                        popUpTo(BikeOSDestinations.SIGNUP) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSubmitting) "Creating account..." else "Create account")
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = BikeTextPrimary)
}
