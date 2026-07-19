package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class AppSettings(
    val useMetricUnits: Boolean = true,
    val soundEnabled: Boolean = true,
    val maxSpeedAlertKmh: Int = 40,
    val isDarkTheme: Boolean = true,
    val use24HourClock: Boolean = true,
    val gearSuggestionsEnabled: Boolean = true,
    val antiTheftAlarmEnabled: Boolean = false,
    val reminderNotificationsEnabled: Boolean = true,
    val engineStartAnimationEnabled: Boolean = true
)

private fun SettingsEntity.toDomain() = AppSettings(
    useMetricUnits, soundEnabled, maxSpeedAlertKmh, isDarkTheme, use24HourClock,
    gearSuggestionsEnabled, antiTheftAlarmEnabled, reminderNotificationsEnabled,
    engineStartAnimationEnabled
)

private fun AppSettings.toEntity() = SettingsEntity(
    0, useMetricUnits, soundEnabled, maxSpeedAlertKmh, isDarkTheme, use24HourClock,
    gearSuggestionsEnabled, antiTheftAlarmEnabled, reminderNotificationsEnabled,
    engineStartAnimationEnabled
)

class SettingsRepository @Inject constructor(
    private val dao: SettingsDao
) {
    fun observe(): Flow<AppSettings> = dao.observe().map { it?.toDomain() ?: AppSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        val current = dao.get()?.toDomain() ?: AppSettings()
        dao.upsert(transform(current).toEntity())
    }

    suspend fun clear() = dao.clear()
}
