package com.voidroot.bikeos.presentation.menu.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

/**
 * Dashboard widget enable/disable toggles + per-role Day/Night cluster
 * color customization (per the UI/UX spec's Widget System and "colors for
 * day vs night" ask). Speed isn't in the widget list - it's the primary
 * cockpit reading and isn't toggleable. Drag-to-reorder for widgets is a
 * later visual-polish pass; stored `position` values already support it.
 */
@Composable
fun AppearanceScreen(viewModel: AppearanceViewModel = hiltViewModel()) {
    val widgets by viewModel.widgets.collectAsStateWithLifecycle()
    val themeColors by viewModel.themeColors.collectAsStateWithLifecycle()
    var selectedMode by remember { mutableStateOf(DayNightMode.NIGHT) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Appearance", style = MaterialTheme.typography.headlineMedium, color = BikeTextPrimary)

        Text(
            "Dashboard widgets",
            style = MaterialTheme.typography.titleMedium,
            color = BikeTextSecondary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        widgets.forEach { widget ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(widget.key.replaceFirstChar { it.uppercase() }, color = BikeTextPrimary)
                Switch(
                    checked = widget.enabled,
                    onCheckedChange = { viewModel.setEnabled(widget.key, it) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        Text(
            "Cluster colors",
            style = MaterialTheme.typography.titleMedium,
            color = BikeTextSecondary
        )
        Text(
            "Day and night colors switch automatically (6am-6pm = day).",
            style = MaterialTheme.typography.labelSmall,
            color = BikeTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
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
            Text(role.label, color = BikeTextPrimary, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorSwatches.forEach { swatch ->
                    val isSelected = palette.valueFor(role) == swatch
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(32.dp)
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
