package com.voidroot.bikeos.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeSuccess
import com.voidroot.bikeos.core.theme.BikeWarning

/**
 * Ride modes per product spec. Selecting a mode is local-only (no
 * gear-suggestion logic tied to it yet - that's a later Smart Features
 * phase). [glyph] is a simple Unicode symbol, not an icon-library
 * dependency, matching the rest of the cluster's hand-drawn visual style.
 */
enum class RideMode(val label: String, val color: Color, val glyph: String) {
    ECO("Eco", BikeSuccess, "\u2601"),
    CRUISE("Cruise", BikePrimary, "\u25CE"),
    SPRINT("Sprint", BikeDanger, "\u26A1"),
    CLIMB("Climb", BikeWarning, "\u25B2"),
    DOWNHILL("Downhill", BikeAccent, "\u2193")
}
