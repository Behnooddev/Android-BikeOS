package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.albumArt != null) {
                    Image(
                        bitmap = state.albumArt.asImageBitmap(),
                        contentDescription = "Album art",
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.size(36.dp).background(palette.cardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        state.title ?: "Not Playing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textPrimary
                    )
                    state.artist?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp).clickable(onClick = onPrevious)
                )
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = palette.primary,
                    modifier = Modifier.size(28.dp).clickable(onClick = onPlayPause)
                )
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp).clickable(onClick = onNext)
                )
            }
        }
    }
}
