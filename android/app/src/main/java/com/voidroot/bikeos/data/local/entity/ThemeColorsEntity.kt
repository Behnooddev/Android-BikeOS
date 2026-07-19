package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User-customizable cluster colors, with independent Day and Night
 * palettes (per the UI/UX ask: "day with these colors, night with better-
 * visibility colors"). Colors are stored as ARGB Long values
 * (android.graphics.Color-compatible ints, widened to Long since Room
 * doesn't have an unsigned Int column type).
 *
 * Day/Night selection itself is time-of-day based (see ThemeRepository),
 * not manually toggled - "Appearance" lets the user pick what each mode
 * looks like, not which one is active right now.
 */
@Entity(tableName = "theme_colors")
data class ThemeColorsEntity(
    @PrimaryKey val id: Int = 0,

    val dayPrimary: Long = 0xFF0077CC,
    val dayAccent: Long = 0xFF7C4DFF,
    val dayBackground: Long = 0xFFF5F7FA,
    val dayCardBackground: Long = 0x33000000,
    val dayTextPrimary: Long = 0xFF0A0E14,

    val nightPrimary: Long = 0xFF00E5FF,
    val nightAccent: Long = 0xFF7C4DFF,
    val nightBackground: Long = 0xFF0A0E14,
    val nightCardBackground: Long = 0x33FFFFFF,
    val nightTextPrimary: Long = 0xFFF5F7FA
)
