package com.voidroot.bikeos.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale. displayLarge is reserved for the main speed number
 * (highest information priority per the UI/UX spec's typography hierarchy).
 */
val BikeOSTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 96.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp)
)
