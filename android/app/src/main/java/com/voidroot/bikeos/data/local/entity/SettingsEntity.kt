package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row app settings table. [id] fixed at 0. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val useMetricUnits: Boolean = true,     // true = km, false = miles
    val soundEnabled: Boolean = true,
    val maxSpeedAlertKmh: Int = 40,
    val isDarkTheme: Boolean = true,
    val use24HourClock: Boolean = true,
    val gearSuggestionsEnabled: Boolean = true,
    val antiTheftAlarmEnabled: Boolean = false,
    val reminderNotificationsEnabled: Boolean = true,
    val engineStartAnimationEnabled: Boolean = true
)
