package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import com.voidroot.bikeos.data.media.MusicState

/**
 * Generic now-playing widget - cover, title, artist, and large tap targets
 * for previous/play-pause/next, controlling whatever media session is
 * currently active on the phone (see MusicRepository).
 */
@Composable
fun MusicWidget(
    state: MusicState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalClusterPalette.current

    GlassCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            state.albumArt?.let { art ->
                Image(
                    bitmap = art.asImageBitmap(),
                    contentDescription = "Album art",
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                )
            }
            Column(modifier = Modifier.padding(start = if (state.albumArt != null) 10.dp else 0.dp)) {
                Text(
                    state.title ?: "Not playing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
                state.artist?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        "⏮",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        modifier = Modifier.clickable(onClick = onPrevious)
                    )
                    Text(
                        if (state.isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.primary,
                        modifier = Modifier.clickable(onClick = onPlayPause)
                    )
                    Text(
                        "⏭",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        modifier = Modifier.clickable(onClick = onNext)
                    )
                }
            }
        }
    }
}
