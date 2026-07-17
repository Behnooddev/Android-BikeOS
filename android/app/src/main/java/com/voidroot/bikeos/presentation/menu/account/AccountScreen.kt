package com.voidroot.bikeos.presentation.menu.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

/**
 * Rider profile form (name/email/username/age/height/weight per the
 * product spec's User Profile section). Used for calories calculation and
 * future recommendations - Room-backed via [AccountViewModel]/[com.voidroot.bikeos.data.repository.UserRepository].
 */
@Composable
fun AccountScreen(viewModel: AccountViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Account", style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)

        OutlinedTextField(
            value = profile.name,
            onValueChange = { v -> viewModel.update { it.copy(name = v) } },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = profile.username,
            onValueChange = { v -> viewModel.update { it.copy(username = v) } },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = profile.email,
            onValueChange = { v -> viewModel.update { it.copy(email = v) } },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = if (profile.age == 0) "" else profile.age.toString(),
            onValueChange = { v -> viewModel.update { it.copy(age = v.toIntOrNull() ?: 0) } },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = if (profile.heightCm == 0) "" else profile.heightCm.toString(),
            onValueChange = { v -> viewModel.update { it.copy(heightCm = v.toIntOrNull() ?: 0) } },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = if (profile.weightKg == 0) "" else profile.weightKg.toString(),
            onValueChange = { v -> viewModel.update { it.copy(weightKg = v.toIntOrNull() ?: 0) } },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
            Text(if (saved) "Saved" else "Save")
        }
        if (saved) {
            Text(
                "Profile saved locally.",
                style = MaterialTheme.typography.labelSmall,
                color = BikeTextSecondary
            )
        }
    }
}
