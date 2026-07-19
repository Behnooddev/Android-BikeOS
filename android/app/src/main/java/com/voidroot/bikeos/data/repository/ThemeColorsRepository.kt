package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.ThemeColorsDao
import com.voidroot.bikeos.data.local.entity.ThemeColorsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ThemePalette(
    val primary: Long,
    val accent: Long,
    val background: Long,
    val cardBackground: Long,
    val textPrimary: Long
)

data class ThemeColors(
    val day: ThemePalette,
    val night: ThemePalette
)

private fun ThemeColorsEntity.toDomain() = ThemeColors(
    day = ThemePalette(dayPrimary, dayAccent, dayBackground, dayCardBackground, dayTextPrimary),
    night = ThemePalette(nightPrimary, nightAccent, nightBackground, nightCardBackground, nightTextPrimary)
)

private fun ThemeColors.toEntity() = ThemeColorsEntity(
    id = 0,
    dayPrimary = day.primary, dayAccent = day.accent, dayBackground = day.background,
    dayCardBackground = day.cardBackground, dayTextPrimary = day.textPrimary,
    nightPrimary = night.primary, nightAccent = night.accent, nightBackground = night.background,
    nightCardBackground = night.cardBackground, nightTextPrimary = night.textPrimary
)

class ThemeColorsRepository @Inject constructor(
    private val dao: ThemeColorsDao
) {
    fun observe(): Flow<ThemeColors> = dao.observe().map { it?.toDomain() ?: ThemeColorsEntity().toDomain() }

    suspend fun save(colors: ThemeColors) = dao.upsert(colors.toEntity())
}
