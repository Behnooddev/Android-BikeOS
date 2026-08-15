package com.voidroot.bikeos.core.theme

import androidx.compose.ui.graphics.Color

/**
 * BikeOS design tokens - Phase I refresh: "modern premium, Apple x Tesla"
 * per the product ask (deep near-black backgrounds, a restrained signal-
 * blue accent instead of neon/gaming cyan, high-contrast near-white text)
 * instead of the earlier neon-cyan "cockpit" baseline.
 *
 * These are the values a future in-app "Theme Engine" (Settings > Appearance,
 * per the UI/UX spec) will read/override at runtime. Kept as plain constants
 * for now; Phase 2+ will move user-selected overrides into DataStore/Room and
 * expose them through a ThemeState, not hardcoded values like these.
 *
 * NOTE: BikeBackground is mirrored in res/values/colors.xml's
 * bike_background (XML resources - the splash screen theme, adaptive icon
 * background - can't read these Kotlin constants directly). If this value
 * changes, update that file too.
 */

// Base - true near-black, blue-tinted (Apple/Tesla dark-mode depth, not a
// flat gray-black) - deep enough to feel premium/OLED, not washed out.
val BikeBackground = Color(0xFF06070A)
val BikeSurface = Color(0xFF0E1015)

// Glassmorphism card tint (drawn with alpha + blur, not a flat fill) -
// slightly richer than before for a cleaner "frosted glass" read.
val BikeGlassTint = Color(0x3DFFFFFF)
val BikeGlassBorder = Color(0x26FFFFFF)

// Primary / accent - a restrained "signal blue" (closer to Apple's
// systemBlue / Tesla's interactive-element blue than a neon/gaming cyan)
// as primary, a deep indigo-violet as the secondary accent for gradients.
val BikePrimary = Color(0xFF3E8EFF)
val BikeAccent = Color(0xFF8B5CF6)

// Status
val BikeSuccess = Color(0xFF30D158)
val BikeWarning = Color(0xFFFFC53D)
val BikeDanger = Color(0xFFFF453A)

// Text - high-contrast near-white primary (not pure #FFFFFF - a hair warmer
// reads less clinical), a cooler muted gray for secondary.
val BikeTextPrimary = Color(0xFFF7F8FA)
val BikeTextSecondary = Color(0xFF9BA3AF)
