package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-bike table for Phase 2. [id] is fixed at 0. The product spec only
 * requires one active bike configuration right now; a future multi-bike
 * feature would add a real autoGenerate PK plus an "active bike" pointer
 * elsewhere - deliberately not built ahead of that requirement existing.
 */
@Entity(tableName = "bike_profile")
data class BikeProfileEntity(
    @PrimaryKey val id: Int = 0,
    val bikeName: String = "My Bike",
    val bikeType: String = "Mountain Bike",
    val wheelSizeInches: Float = 27.5f,
    val frontGearCount: Int = 1,
    val rearGearCount: Int = 1,
    val currentFrontGear: Int = 1,
    val currentRearGear: Int = 1
)
