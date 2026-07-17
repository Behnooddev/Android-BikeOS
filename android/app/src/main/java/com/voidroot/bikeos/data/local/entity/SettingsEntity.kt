package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row app settings table. [id] fixed at 0.
 * Theme customization fields (primary/accent color overrides) are left out
 * until Phase 2's Appearance screen actually needs them - only Units/Sound/
 * Alert preferences are wired to real UI this phase.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val useMetricUnits: Boolean = true,     // true = km, false = miles
    val soundEnabled: Boolean = true,
    val maxSpeedAlertKmh: Int = 40
)
