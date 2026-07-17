package com.voidroot.bikeos.core.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.BikeGlassBorder
import com.voidroot.bikeos.core.theme.BikeGlassTint

/**
 * Shared glassmorphism container used by every dashboard widget card.
 *
 * Phase 0: flat semi-transparent fill + thin border (approximates the glass
 * look). Real background blur (Modifier.blur / RenderEffect, Android 12+
 * with a graceful fallback below API 31) is a Phase 1 visual-polish item -
 * intentionally not implemented yet so this component's public API doesn't
 * change once blur is added.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(BikeGlassTint, RoundedCornerShape(20.dp))
            .border(1.dp, BikeGlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        content()
    }
}
