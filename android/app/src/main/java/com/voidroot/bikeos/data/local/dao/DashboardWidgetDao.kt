package com.voidroot.bikeos.data.local.dao

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.Query
import com.voidroot.bikeos.data.local.entity.DashboardWidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardWidgetDao {
    @Query("SELECT * FROM dashboard_widget ORDER BY position ASC")
    fun observeAll(): Flow<List<DashboardWidgetEntity>>

    @Query("SELECT * FROM dashboard_widget ORDER BY position ASC")
    suspend fun getAll(): List<DashboardWidgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<DashboardWidgetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DashboardWidgetEntity)

    @Query("DELETE FROM dashboard_widget")
    suspend fun clear()
}
