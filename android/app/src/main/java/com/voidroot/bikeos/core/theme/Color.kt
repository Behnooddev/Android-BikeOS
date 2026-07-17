package com.voidroot.bikeos.core.theme

import androidx.compose.ui.graphics.Color

/**
 * BikeOS design tokens - Phase 0 baseline palette.
 *
 * These are the values a future in-app "Theme Engine" (Settings > Appearance,
 * per the UI/UX spec) will read/override at runtime. Kept as plain constants
 * for now; Phase 2+ will move user-selected overrides into DataStore/Room and
 * expose them through a ThemeState, not hardcoded values like these.
 */

// Base - dark, futuristic cockpit background
val BikeBackground = Color(0xFF0A0E14)
val BikeSurface = Color(0xFF12161F)

// Glassmorphism card tint (drawn with alpha + blur, not a flat fill)
val BikeGlassTint = Color(0x33FFFFFF)
val BikeGlassBorder = Color(0x22FFFFFF)

// Primary / accent - electric cyan, matches "premium automotive" direction
val BikePrimary = Color(0xFF00E5FF)
val BikeAccent = Color(0xFF7C4DFF)

// Status
val BikeSuccess = Color(0xFF00E676)
val BikeWarning = Color(0xFFFFC400)
val BikeDanger = Color(0xFFFF5252)

// Text
val BikeTextPrimary = Color(0xFFF5F7FA)
val BikeTextSecondary = Color(0xFF9AA5B1)
