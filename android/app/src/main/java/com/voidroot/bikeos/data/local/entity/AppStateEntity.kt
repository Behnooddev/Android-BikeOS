package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table for app-lifecycle state that ISN'T a user preference
 * (that's [SettingsEntity]) and isn't profile data (that's
 * [UserProfileEntity]) - kept separate so "Erase Data" can reset onboarding
 * without needing to know about every settings field, and so Settings
 * screens don't accidentally expose these as editable toggles.
 *
 * [avgRideStartMinuteOfDay]/[rideStartSampleCount]: a running average
 * (Welford-style incremental mean) of the minute-of-day the user has
 * pressed Start Ride historically - used to time the "haven't ridden
 * today" reminder notification near when the user usually rides, not at
 * an arbitrary fixed hour.
 */
@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val id: Int = 0,
    val hasCompletedOnboarding: Boolean = false,
    val hasCompletedSignup: Boolean = false,
    val lastAppOpenEpochMs: Long = 0L,
    val lastRideStartEpochDay: Long = -1L, // days since epoch - "did we already ride today?"
    val avgRideStartMinuteOfDay: Int = 480, // default guess: 8:00 AM, before any real samples
    val rideStartSampleCount: Int = 0
)
