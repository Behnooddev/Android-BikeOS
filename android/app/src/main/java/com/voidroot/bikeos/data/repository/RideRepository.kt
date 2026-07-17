package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.RideSessionDao
import com.voidroot.bikeos.data.local.entity.RideSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RideSession(
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

private fun RideSessionEntity.toDomain() = RideSession(
    startTimeEpochMs, endTimeEpochMs, durationSeconds, distanceKm, calories,
    avgSpeedKmh, maxSpeedKmh, avgCadenceRpm, maxCadenceRpm, rideMode
)

private fun RideSession.toEntity() = RideSessionEntity(
    startTimeEpochMs = startTimeEpochMs,
    endTimeEpochMs = endTimeEpochMs,
    durationSeconds = durationSeconds,
    distanceKm = distanceKm,
    calories = calories,
    avgSpeedKmh = avgSpeedKmh,
    maxSpeedKmh = maxSpeedKmh,
    avgCadenceRpm = avgCadenceRpm,
    maxCadenceRpm = maxCadenceRpm,
    rideMode = rideMode
)

/**
 * Writes happen exactly once, at ride end - never per-tick. See the
 * entity's kdoc for why (Data Architecture spec: live data must not
 * overload permanent storage).
 */
class RideRepository @javax.inject.Inject constructor(
    private val dao: RideSessionDao
) {
    fun observeRecent(limit: Int = 20): Flow<List<RideSession>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    suspend fun saveCompletedRide(ride: RideSession) {
        dao.insert(ride.toEntity())
    }
}
