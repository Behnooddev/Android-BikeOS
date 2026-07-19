package com.voidroot.bikeos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voidroot.bikeos.data.local.entity.ThemeColorsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeColorsDao {
    @Query("SELECT * FROM theme_colors WHERE id = 0 LIMIT 1")
    fun observe(): Flow<ThemeColorsEntity?>

    @Query("SELECT * FROM theme_colors WHERE id = 0 LIMIT 1")
    suspend fun get(): ThemeColorsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ThemeColorsEntity)
}
