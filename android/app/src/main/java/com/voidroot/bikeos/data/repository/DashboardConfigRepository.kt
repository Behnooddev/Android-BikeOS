package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.DashboardWidgetDao
import com.voidroot.bikeos.data.local.entity.DashboardWidgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Stable string keys for dashboard widgets - NOT an enum ordinal, so adding
 * a widget later never shifts existing stored rows. Distance/Calories/
 * Cadence/Gear map directly to the glass cards already on the Phase 1
 * dashboard; Speed is intentionally not toggleable (it's the primary
 * cockpit reading, hiding it doesn't make sense for this product).
 */
object WidgetKeys {
    const val DISTANCE = "distance"
    const val CALORIES = "calories"
    const val CADENCE = "cadence"
    const val GEAR = "gear"

    val DEFAULT_ORDER = listOf(DISTANCE, CALORIES, CADENCE, GEAR)
}

data class DashboardWidget(val key: String, val position: Int, val enabled: Boolean)

private fun DashboardWidgetEntity.toDomain() = DashboardWidget(widgetKey, position, enabled)

class DashboardConfigRepository @Inject constructor(
    private val dao: DashboardWidgetDao
) {
    /** Emits the seeded defaults on first read of a fresh install. */
    fun observeWidgets(): Flow<List<DashboardWidget>> =
        dao.observeAll().map { rows ->
            if (rows.isEmpty()) defaultWidgets() else rows.map { it.toDomain() }
        }

    suspend fun ensureSeeded() {
        if (dao.getAll().isEmpty()) {
            dao.upsertAll(
                WidgetKeys.DEFAULT_ORDER.mapIndexed { index, key ->
                    DashboardWidgetEntity(widgetKey = key, position = index, enabled = true)
                }
            )
        }
    }

    suspend fun setEnabled(key: String, enabled: Boolean) {
        val current = dao.getAll().find { it.widgetKey == key }
            ?: DashboardWidgetEntity(key, WidgetKeys.DEFAULT_ORDER.indexOf(key), enabled)
        dao.upsert(current.copy(enabled = enabled))
    }

    private fun defaultWidgets() = WidgetKeys.DEFAULT_ORDER.mapIndexed { index, key ->
        DashboardWidget(key, index, enabled = true)
    }
}
