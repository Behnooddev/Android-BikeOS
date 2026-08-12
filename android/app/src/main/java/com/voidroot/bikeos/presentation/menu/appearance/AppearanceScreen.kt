package com.voidroot.bikeos.presentation.menu.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.BikeOSMenuScaffold

/**
 * Dashboard widget enable/disable toggles + per-role Day/Night cluster
 * color customization. Speed isn't in the widget list - it's the primary
 * cockpit reading and isn't toggleable.
 */
@Composable
fun AppearanceScreen(navController: NavHostController, viewModel: AppearanceViewModel = hiltViewModel()) {
    val widgets by viewModel.widgets.collectAsStateWithLifecycle()
    val themeColors by viewModel.themeColors.collectAsStateWithLifecycle()
    var selectedMode by remember { mutableStateOf(DayNightMode.NIGHT) }

    BikeOSMenuScaffold(navController, "Appearance") {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dashboard Widgets", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = BikeTextPrimary)
                widgets.forEach { widget ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(widget.key.replaceFirstChar { it.uppercase() }, color = BikeTextPrimary)
                        Switch(checked = widget.enabled, onCheckedChange = { viewModel.setEnabled(widget.key, it) })
                    }
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Cluster Colors", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = BikeTextPrimary)
                Text(
                    "Day and night colors switch automatically (6am-6pm = day).",
                    style = MaterialTheme.typography.labelSmall,
                    color = BikeTextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DayNightMode.entries.forEach { mode ->
                        val isSelected = mode == selectedMode
                        Text(
                            text = if (mode == DayNightMode.DAY) "Day" else "Night",
                            color = if (isSelected) BikeTextPrimary else BikeTextSecondary,
                            style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { selectedMode = mode }
                        )
                    }
                }

                val palette = if (selectedMode == DayNightMode.DAY) themeColors.day else themeColors.night

                ColorRole.entries.forEach { role ->
                    Text(role.label, color = BikeTextPrimary, style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorSwatches.forEach { swatch ->
                            val isSelected = palette.valueFor(role) == swatch
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(swatch.toInt()), CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) BikeTextPrimary else BikeTextSecondary.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setColor(selectedMode, role, swatch) }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
