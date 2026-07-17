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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeGlassBorder
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeTextPrimary
import com.voidroot.bikeos.core.theme.BikeTextSecondary

/**
 * Semi-circular (270°) speed gauge with a digital number in the center.
 *
 * The fill fraction and the digital number both animate through a low-
 * stiffness spring rather than snapping instantly, which is what gives the
 * "liquid rising/falling" feel the UI spec asks for. A true fluid-shader
 * effect (wave motion inside the fill) is left as a later visual-polish
 * pass - this gets the smooth, non-instant behavior the spec requires
 * without the complexity of a custom shader.
 */
@Composable
fun SpeedGauge(
    speedKmh: Float,
    maxSpeedKmh: Float,
    modifier: Modifier = Modifier
) {
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

    Box(modifier = modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val strokeWidth = 22.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val startAngle = 135f
            val maxSweep = 270f
            val fillSweep = maxSweep * animatedFraction

            // background track
            drawArc(
                color = BikeGlassBorder,
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // soft glow behind the fill - two wider, fainter strokes
            drawArc(
                color = BikePrimary.copy(alpha = 0.12f),
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth + 18f, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            drawArc(
                color = BikePrimary.copy(alpha = 0.25f),
                startAngle = startAngle,
                sweepAngle = fillSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth + 8f, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // the actual fill
            drawArc(
                brush = Brush.sweepGradient(listOf(BikePrimary, BikeAccent, BikePrimary)),
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
                style = MaterialTheme.typography.displayLarge,
                color = BikeTextPrimary
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.titleMedium,
                color = BikeTextSecondary
            )
        }
    }
}
