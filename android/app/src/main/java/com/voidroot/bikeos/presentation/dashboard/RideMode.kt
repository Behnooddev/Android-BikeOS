package com.voidroot.bikeos.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.voidroot.bikeos.core.theme.BikeAccent
import com.voidroot.bikeos.core.theme.BikeDanger
import com.voidroot.bikeos.core.theme.BikePrimary
import com.voidroot.bikeos.core.theme.BikeSuccess
import com.voidroot.bikeos.core.theme.BikeWarning

/**
 * Ride modes per product spec. Selecting a mode is local-only in Phase 1
 * (no gear-suggestion logic, no persistence yet - those land in Phase 5
 * and Phase 2 respectively).
 */
enum class RideMode(val label: String, val color: Color) {
    ECO("Eco", BikeSuccess),
    CRUISE("Cruise", BikePrimary),
    SPRINT("Sprint", BikeDanger),
    CLIMB("Climb", BikeWarning),
    DOWNHILL("Downhill", BikeAccent)
}
