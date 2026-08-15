package com.voidroot.bikeos.presentation.dashboard.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.LocalClusterPalette
import kotlin.math.cos
import kotlin.math.sin

/**
 * Semi-circular (270°) speed gauge with a digital number in the center.
 * Colors come from [LocalClusterPalette] so day/night cluster customization
 * applies here too.
 *
 * The fill fraction and the digital number both animate through a low-
 * stiffness spring rather than snapping instantly, which is what gives the
 * "liquid rising/falling" feel the UI spec asks for. A soft radial glow
 * sits behind the whole gauge for a bit more depth/premium feel.
 *
 * Phase I: added instrument-cluster-style tick marks around the arc (the
 * "BMW multimedia cluster" reference theme - a real gauge reads speed
 * against fixed graduations, not just a bare progress ring) and switched
 * the digital readout to a monospace font, which is what gives premium
 * automotive/aviation instrument readouts their "precision tool" feel
 * versus a generic proportional UI font.
 */
@Composable
fun SpeedGauge(
    speedKmh: Float,
    maxSpeedKmh: Float,
    modifier: Modifier = Modifier
) {
    val palette = LocalClusterPalette.current
    val targetFraction = (speedKmh / maxSpeedKmh).coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "speedGaugeFill"
    )
    val animatedDisplaySpeed by animateFloatAsState(
        targetValue = speedKmh,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "speedNumber"
    )

    Box(modifier = modifier.size(220.dp), contentAlignment = Alignment.Center) {
        // Soft ambient glow behind the gauge - purely decorative depth.
        Canvas(modifier = Modifier.size(220.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(palette.primary.copy(alpha = 0.10f), palette.primary.copy(alpha = 0f))
                ),
                radius = size.minDimension / 2f
            )
        }

        // Tick marks - 12 graduations across the 270° sweep (every ~24.5°),
        // every 4th tick drawn longer/brighter as a "major" mark, matching
        // how a real instrument cluster reads at a glance without needing
        // numbers printed at every graduation.
        Canvas(modifier = Modifier.size(220.dp)) {
            val startAngleDeg = 135f
            val sweepDeg = 270f
            val tickCount = 12
            val outerRadius = size.minDimension / 2f - 2.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            for (i in 0..tickCount) {
                val angleDeg = startAngleDeg + sweepDeg * (i / tickCount.toFloat())
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val isMajor = i % 4 == 0
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val innerRadius = outerRadius - tickLength

                val outer = Offset(
                    center.x + (outerRadius * cos(angleRad)).toFloat(),
                    center.y + (outerRadius * sin(angleRad)).toFloat()
                )
                val inner = Offset(
                    center.x + (innerRadius * cos(angleRad)).toFloat(),
                    center.y + (innerRadius * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = if (isMajor) palette.textSecondary.copy(alpha = 0.55f) else palette.textSecondary.copy(alpha = 0.25f),
                    start = inner,
                    end = outer,
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 18.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val startAngle = 135f
            val maxSweep = 270f
            val fillSweep = maxSweep * animatedFraction

            drawArc(
                color = palette.cardBorder,
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            drawArc(
                color = palette.primary.copy(alpha = 0.12f),
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth + 18f, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            drawArc(
                color = palette.primary.copy(alpha = 0.25f),
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth + 8f, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            drawArc(
                brush = Brush.sweepGradient(listOf(palette.primary, palette.accent, palette.primary)),
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = animatedDisplaySpeed.toInt().toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Monospace),
                color = palette.textPrimary
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textSecondary
            )
        }
    }
}
