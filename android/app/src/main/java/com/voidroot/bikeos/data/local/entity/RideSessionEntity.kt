package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One completed ride. Written once at ride end (see [bikeos.data.repository.RideRepository]),
 * never per-tick - high-frequency live values stay in memory during the ride
 * (per the Data Architecture spec's "Live Ride Data... should not overload
 * permanent storage") and only the aggregate lands here.
 */
@Entity(tableName = "ride_session")
data class RideSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationSeconds: Long,
    val distanceKm: Float,
    val calories: Int,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val avgCadenceRpm: Int,
    val maxCadenceRpm: Int,
    val rideMode: String
)
