package com.voidroot.bikeos.presentation.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.moveTo
import androidx.compose.ui.graphics.quadraticTo
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Deliberately simple, hand-drawn Canvas glyphs rather than an icon library
 * dependency - each onboarding page gets one large, friendly shape instead
 * of stock iconography, which reads as more custom/branded for a "make it
 * feel creative" first-run experience.
 */
@Composable
fun OnboardingGlyphIcon(glyph: OnboardingGlyph, tint: Color) {
    Canvas(modifier = Modifier.size(160.dp)) {
        val stroke = Stroke(width = 10f, cap = StrokeCap.Round)
        val c = center

        when (glyph) {
            OnboardingGlyph.SPEEDOMETER -> {
                drawArc(tint.copy(alpha = 0.9f), 135f, 270f, false, style = stroke)
                drawLine(tint, c, Offset(c.x + 40f, c.y - 60f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawCircle(tint, radius = 10f, center = c)
            }
            OnboardingGlyph.BLUETOOTH_LINK -> {
                val w = size.width * 0.18f
                val top = Offset(c.x, c.y - 60f)
                val bottom = Offset(c.x, c.y + 60f)
                drawLine(tint, top, bottom, strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, top, Offset(c.x + w, c.y - 20f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, bottom, Offset(c.x + w, c.y + 20f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x - w, c.y - 20f), Offset(c.x + w, c.y + 20f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x - w, c.y + 20f), Offset(c.x + w, c.y - 20f), strokeWidth = 8f, cap = StrokeCap.Round)
            }
            OnboardingGlyph.SHIELD_BOLT -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(c.x, c.y - 70f)
                    lineTo(c.x + 55f, c.y - 40f)
                    lineTo(c.x + 55f, c.y + 20f)
                    quadraticTo(c.x + 55f, c.y + 65f, c.x, c.y + 80f)
                    quadraticTo(c.x - 55f, c.y + 65f, c.x - 55f, c.y + 20f)
                    lineTo(c.x - 55f, c.y - 40f)
                    close()
                }
                drawPath(path, tint.copy(alpha = 0.15f))
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(c.x + 10f, c.y - 25f), Offset(c.x - 10f, c.y + 10f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x - 10f, c.y + 10f), Offset(c.x + 15f, c.y + 10f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x + 15f, c.y + 10f), Offset(c.x - 5f, c.y + 45f), strokeWidth = 8f, cap = StrokeCap.Round)
            }
            OnboardingGlyph.PALETTE -> {
                drawCircle(tint.copy(alpha = 0.9f), radius = 24f, center = Offset(c.x - 40f, c.y - 20f))
                drawCircle(tint.copy(alpha = 0.7f), radius = 24f, center = Offset(c.x + 10f, c.y - 45f))
                drawCircle(tint.copy(alpha = 0.5f), radius = 24f, center = Offset(c.x + 45f, c.y))
                drawCircle(tint.copy(alpha = 0.3f), radius = 24f, center = Offset(c.x, c.y + 40f))
            }
            OnboardingGlyph.BIKE -> {
                val r = 34f
                drawCircle(tint, radius = r, center = Offset(c.x - 50f, c.y + 30f), style = stroke)
                drawCircle(tint, radius = r, center = Offset(c.x + 50f, c.y + 30f), style = stroke)
                drawLine(tint, Offset(c.x - 50f, c.y + 30f), Offset(c.x - 5f, c.y - 20f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x - 5f, c.y - 20f), Offset(c.x + 50f, c.y + 30f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x - 5f, c.y - 20f), Offset(c.x + 20f, c.y - 20f), strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x - 50f, c.y + 30f), Offset(c.x - 15f, c.y - 15f), strokeWidth = 8f, cap = StrokeCap.Round)
            }
        }
    }
}
