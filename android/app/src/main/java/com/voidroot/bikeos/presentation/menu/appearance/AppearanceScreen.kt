package com.voidroot.bikeos.presentation.menu.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

/**
 * Dashboard widget enable/disable toggles (per the UI/UX spec's Widget
 * System). Speed isn't listed - it's the primary cockpit reading and isn't
 * toggleable. Drag-to-reorder is a later visual-polish pass; stored
 * `position` values already support it, this screen just doesn't expose
 * reordering UI yet.
 */
@Composable
fun AppearanceScreen(viewModel: AppearanceViewModel = hiltViewModel()) {
    val widgets by viewModel.widgets.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
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
    }
}
