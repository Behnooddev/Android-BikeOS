package com.voidroot.bikeos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.voidroot.bikeos.data.local.entity.RideSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideSessionDao {
    @Query("SELECT * FROM ride_session ORDER BY startTimeEpochMs DESC")
    fun observeAll(): Flow<List<RideSessionEntity>>

    @Query("SELECT * FROM ride_session ORDER BY startTimeEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<RideSessionEntity>>

    @Query("SELECT * FROM ride_session ORDER BY startTimeEpochMs DESC")
    suspend fun getAllOnce(): List<RideSessionEntity>

    @Insert
    suspend fun insert(entity: RideSessionEntity): Long
}
