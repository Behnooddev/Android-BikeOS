package com.voidroot.bikeos.presentation.menu.appearance

enum class DayNightMode { DAY, NIGHT }
enum class ColorRole(val label: String) {
    PRIMARY("Primary"), ACCENT("Accent"), BACKGROUND("Background"),
    CARD_BACKGROUND("Card background"), TEXT_PRIMARY("Text")
}

/**
 * Curated swatch palette for the Appearance color pickers. Not a full
 * HSV/RGB picker (would need either a custom-built color wheel or a third-
 * party library) - a fixed, sensible set of choices is a reasonable v1 and
 * avoids shipping a picker UI that's fiddly to use one-handed on a phone
 * mounted to a handlebar anyway.
 */
val colorSwatches: List<Long> = listOf(
    0xFF00E5FF, 0xFF7C4DFF, 0xFF00E676, 0xFFFFC400, 0xFFFF5252,
    0xFF2979FF, 0xFFFF4081, 0xFFFFEA00, 0xFF00BFA5, 0xFFFFFFFF,
    0xFF0A0E14, 0xFF9AA5B1
)

fun com.voidroot.bikeos.data.repository.ThemePalette.valueFor(role: ColorRole): Long = when (role) {
    ColorRole.PRIMARY -> primary
    ColorRole.ACCENT -> accent
    ColorRole.BACKGROUND -> background
    ColorRole.CARD_BACKGROUND -> cardBackground
    ColorRole.TEXT_PRIMARY -> textPrimary
}
