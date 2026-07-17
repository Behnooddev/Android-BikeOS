package com.voidroot.bikeos.data.local.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import com.voidroot.bikeos.data.local.entity.BikeProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeProfileDao {
    @Query("SELECT * FROM bike_profile WHERE id = 0 LIMIT 1")
    fun observe(): Flow<BikeProfileEntity?>

    @Query("SELECT * FROM bike_profile WHERE id = 0 LIMIT 1")
    suspend fun get(): BikeProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BikeProfileEntity)
}
