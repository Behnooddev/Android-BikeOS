package com.voidroot.bikeos.presentation.menu.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.MenuScreenHeader

/**
 * Profile edit screen - same fields collected at signup (minus password,
 * which this screen never touches - see UserRepository.save). Redesigned
 * as an avatar header + two grouped cards (Personal Info / Physical
 * Stats) instead of one long flat list of text fields.
 */
@Composable
fun AccountScreen(navController: NavHostController, viewModel: AccountViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MenuScreenHeader("Profile", navController)

        Row(verticalAlignment = Alignment.CenterVertically) {
            val initials = (profile.firstName.firstOrNull()?.toString() ?: "") +
                (profile.lastName.firstOrNull()?.toString() ?: "")
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(listOf(BikePrimary, BikeAccent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials.ifBlank { "?" }.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BikeBackground
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    profile.fullName.ifBlank { "Unnamed rider" },
                    style = MaterialTheme.typography.titleMedium,
                    color = BikeTextPrimary
                )
                Text("@${profile.username.ifBlank { "username" }}", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Personal Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = BikeTextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = profile.firstName,
                        onValueChange = { v -> viewModel.update { it.copy(firstName = v) } },
                        label = { Text("First name") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = profile.lastName,
                        onValueChange = { v -> viewModel.update { it.copy(lastName = v) } },
                        label = { Text("Last name") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
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
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Physical Stats",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = BikeTextPrimary
                )
                Text(
                    "Used to personalize calorie estimates and future ride recommendations.",
                    style = MaterialTheme.typography.labelSmall,
                    color = BikeTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = if (profile.age == 0) "" else profile.age.toString(),
                        onValueChange = { v -> viewModel.update { it.copy(age = v.toIntOrNull() ?: 0) } },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = if (profile.heightCm == 0) "" else profile.heightCm.toString(),
                        onValueChange = { v -> viewModel.update { it.copy(heightCm = v.toIntOrNull() ?: 0) } },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    OutlinedTextField(
                        value = if (profile.weightKg == 0) "" else profile.weightKg.toString(),
                        onValueChange = { v -> viewModel.update { it.copy(weightKg = v.toIntOrNull() ?: 0) } },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                }
            }
        }

        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
            Text(if (saved) "Saved" else "Save changes")
        }
        if (saved) {
            Text("Profile saved locally.", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
        }
    }
}
