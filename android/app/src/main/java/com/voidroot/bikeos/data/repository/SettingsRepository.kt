package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class AppSettings(
    val useMetricUnits: Boolean = true,
    val soundEnabled: Boolean = true,
    val maxSpeedAlertKmh: Int = 40
)

private fun SettingsEntity.toDomain() = AppSettings(useMetricUnits, soundEnabled, maxSpeedAlertKmh)
private fun AppSettings.toEntity() = SettingsEntity(0, useMetricUnits, soundEnabled, maxSpeedAlertKmh)

class SettingsRepository @Inject constructor(
    private val dao: SettingsDao
) {
    fun observe(): Flow<AppSettings> = dao.observe().map { it?.toDomain() ?: AppSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        val current = dao.get()?.toDomain() ?: AppSettings()
        dao.upsert(transform(current).toEntity())
    }
}
