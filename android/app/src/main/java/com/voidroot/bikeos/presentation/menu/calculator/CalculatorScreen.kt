package com.voidroot.bikeos.presentation.menu.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.BikeOSMenuScaffold

/**
 * A bike-specific calculator: pick what to calculate, fill in the inputs
 * it needs, get a result AND a short reaction comment based on the
 * result's magnitude - per the product spec ("calculate a distance,
 * app reacts: wow that's long").
 */
@Composable
fun CalculatorScreen(navController: NavHostController, viewModel: CalculatorViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BikeOSMenuScaffold(navController, "Calculator") {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalculatorMode.entries.forEach { mode ->
                    val isSelected = mode == state.mode
                    Text(
                        text = mode.label,
                        style = if (isSelected) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.labelSmall,
                        color = if (isSelected) BikeTextPrimary else BikeTextSecondary,
                        modifier = Modifier
                            .background(if (isSelected) BikeAccent.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(50))
                            .clickable { viewModel.setMode(mode) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (state.mode) {
                        CalculatorMode.SPEED_DISTANCE_TIME -> SpeedDistanceTimeInputs(state, viewModel)
                        CalculatorMode.GEAR_RATIO -> GearRatioInputs(state, viewModel)
                        CalculatorMode.CALORIES -> CaloriesInputs(state, viewModel)
                    }

                    Button(onClick = viewModel::calculate, modifier = Modifier.fillMaxWidth()) {
                        Text("Calculate")
                    }
                }
            }

            state.errorMessage?.let {
                Text(it, color = BikeDanger, style = MaterialTheme.typography.labelSmall)
            }

            if (state.resultValue != null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(state.resultTitle ?: "", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
                        Text(state.resultValue ?: "", style = MaterialTheme.typography.headlineMedium, color = BikePrimary)
                        state.reaction?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = BikeTextPrimary, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedDistanceTimeInputs(state: CalculatorUiState, viewModel: CalculatorViewModel) {
    Text("Solve for:", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SolveFor.entries.forEach { option ->
            val isSelected = option == state.solveFor
            Text(
                option.label,
                color = if (isSelected) BikeTextPrimary else BikeTextSecondary,
                style = if (isSelected) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { viewModel.setSolveFor(option) }
            )
        }
    }

    if (state.solveFor != SolveFor.DISTANCE) {
        OutlinedTextField(
            value = state.distanceKm,
            onValueChange = { v -> viewModel.update { it.copy(distanceKm = v) } },
            label = { Text("Distance (km)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (state.solveFor != SolveFor.TIME) {
        OutlinedTextField(
            value = state.timeMinutes,
            onValueChange = { v -> viewModel.update { it.copy(timeMinutes = v) } },
            label = { Text("Time (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (state.solveFor != SolveFor.SPEED) {
        OutlinedTextField(
            value = state.speedKmh,
            onValueChange = { v -> viewModel.update { it.copy(speedKmh = v) } },
            label = { Text("Speed (km/h)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GearRatioInputs(state: CalculatorUiState, viewModel: CalculatorViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.frontTeeth,
            onValueChange = { v -> viewModel.update { it.copy(frontTeeth = v) } },
            label = { Text("Front teeth") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        OutlinedTextField(
            value = state.rearTeeth,
            onValueChange = { v -> viewModel.update { it.copy(rearTeeth = v) } },
            label = { Text("Rear teeth") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
    OutlinedTextField(
        value = state.wheelSizeInches,
        onValueChange = { v -> viewModel.update { it.copy(wheelSizeInches = v) } },
        label = { Text("Wheel size (inches)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.cadenceRpm,
        onValueChange = { v -> viewModel.update { it.copy(cadenceRpm = v) } },
        label = { Text("Cadence (rpm) - optional, for speed") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CaloriesInputs(state: CalculatorUiState, viewModel: CalculatorViewModel) {
    OutlinedTextField(
        value = state.caloriesSpeedKmh,
        onValueChange = { v -> viewModel.update { it.copy(caloriesSpeedKmh = v) } },
        label = { Text("Average speed (km/h)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.caloriesWeightKg,
        onValueChange = { v -> viewModel.update { it.copy(caloriesWeightKg = v) } },
        label = { Text("Weight (kg)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.caloriesDurationMinutes,
        onValueChange = { v -> viewModel.update { it.copy(caloriesDurationMinutes = v) } },
        label = { Text("Duration (minutes)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}
