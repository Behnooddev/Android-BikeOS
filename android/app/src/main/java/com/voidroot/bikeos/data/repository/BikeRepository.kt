package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.BikeProfileDao
import com.voidroot.bikeos.data.local.entity.BikeProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class BikeProfile(
    val bikeName: String = "My Bike",
    val bikeType: String = "Mountain Bike",
    val wheelSizeInches: Float = 27.5f,
    val frontGearCount: Int = 1,
    val rearGearCount: Int = 1,
    val currentFrontGear: Int = 1,
    val currentRearGear: Int = 1
) {
    /** front x rear, per the product spec's gear-combination model. */
    val totalGearCombinations: Int get() = frontGearCount * rearGearCount
}

private fun BikeProfileEntity.toDomain() = BikeProfile(
    bikeName, bikeType, wheelSizeInches, frontGearCount, rearGearCount, currentFrontGear, currentRearGear
)

private fun BikeProfile.toEntity() = BikeProfileEntity(
    0, bikeName, bikeType, wheelSizeInches, frontGearCount, rearGearCount, currentFrontGear, currentRearGear
)

class BikeRepository @Inject constructor(
    private val dao: BikeProfileDao
) {
    fun observe(): Flow<BikeProfile> = dao.observe().map { it?.toDomain() ?: BikeProfile() }

    suspend fun save(profile: BikeProfile) = dao.upsert(profile.toEntity())

    /**
     * Manual gear sync (per spec - Version 1 has no automatic gear sensor).
     * Clamped to the configured gear counts so a stray value can never point
     * at a gear combination the bike doesn't have.
     */
    suspend fun syncCurrentGear(front: Int, rear: Int) {
        val current = dao.get() ?: BikeProfileEntity()
        dao.upsert(
            current.copy(
                currentFrontGear = front.coerceIn(1, current.frontGearCount.coerceAtLeast(1)),
                currentRearGear = rear.coerceIn(1, current.rearGearCount.coerceAtLeast(1))
            )
        )
    }

    suspend fun clear() = dao.clear()
}
