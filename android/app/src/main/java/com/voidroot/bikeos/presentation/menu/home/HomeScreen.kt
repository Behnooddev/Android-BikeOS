package com.voidroot.bikeos.presentation.menu.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.voidroot.bikeos.core.common.GlassCard
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeBackground
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary
import com.voidroot.bikeos.presentation.common.BikeOSMenuScaffold

/**
 * The permanent landing screen from the second app open onward (Splash
 * routes here once onboarding+signup are done). The Start button is the
 * ONLY way into the cluster/Dashboard - everything else here is
 * informational or navigation.
 *
 * Also requests POST_NOTIFICATIONS (API 33+ only) once, here rather than
 * buried in Settings, since ride reminders are an app-wide feature the
 * rider benefits from either way - ReminderWorker itself still re-checks
 * the permission before ever actually showing anything, so declining here
 * doesn't break anything else.
 */
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op either way - ReminderWorker re-checks before showing anything */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Subtle breathing glow behind the Start button - draws the eye without being distracting.
    val glowTransition = rememberInfiniteTransition(label = "startGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    BikeOSMenuScaffold(navController, "BikeOS") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BikeBackground, BikeBackground.copy(alpha = 0.85f))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Decorative readiness ring - echoes the total-distance card below
                    // in a single glanceable number, aviation-instrument style.
                    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(220.dp)) {
                            val strokeWidth = 5.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)
                            drawArc(
                                color = BikeTextPrimary.copy(alpha = 0.06f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )
                            val progressSweep = (uiState.totalDistanceKm.coerceIn(0f, 100f) / 100f) * 360f
                            drawArc(
                                color = BikePrimary,
                                startAngle = -90f,
                                sweepAngle = progressSweep.coerceAtLeast(6f),
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = topLeft,
                                size = arcSize
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format("%.1f", uiState.totalDistanceKm),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                                color = BikeTextPrimary
                            )
                            Text("km", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
                        }
                    }

                    Text(
                        text = uiState.greetingMessage,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
                        color = BikeTextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 32.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(56.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = BikePrimary.copy(alpha = glowAlpha),
                                spotColor = BikeAccent.copy(alpha = glowAlpha)
                            )
                            .background(Brush.horizontalGradient(listOf(BikePrimary, BikeAccent)), RoundedCornerShape(28.dp))
                            .clickable { navController.navigate(BikeOSDestinations.CLUSTER_BOOT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = BikeBackground)
                            Text(
                                "START RIDE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BikeBackground
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Route, contentDescription = null, tint = BikeTextSecondary, modifier = Modifier.size(14.dp))
                            Text("Total Distance", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
                        }
                        Text(
                            String.format("%.1f km", uiState.totalDistanceKm),
                            style = MaterialTheme.typography.titleMedium,
                            color = BikeTextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.DirectionsBike, contentDescription = null, tint = BikeTextSecondary, modifier = Modifier.size(14.dp))
                            Text("Riding Style", style = MaterialTheme.typography.labelSmall, color = BikeTextSecondary)
                        }
                        Text(
                            uiState.ridingStyleSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BikeTextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
    }
}
