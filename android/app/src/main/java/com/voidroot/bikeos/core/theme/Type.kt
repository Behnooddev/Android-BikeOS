package com.voidroot.bikeos.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale - Phase I refresh for the "Apple x Tesla premium"
 * direction: tight negative tracking on the big display number (reads
 * more like a cluster/instrument readout, less like a generic Android
 * headline), a touch of positive tracking on labels (the small-caps-ish
 * "spaced out" look premium automotive/Apple UIs use for secondary
 * labels). displayLarge is reserved for the main speed number (highest
 * information priority per the UI/UX spec's typography hierarchy).
 */
val BikeOSTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 96.sp, letterSpacing = (-2).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, letterSpacing = (-0.5).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.6.sp)
)
