package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per dashboard widget. [widgetKey] is a stable string id (not an
 * enum ordinal) so adding future widgets never breaks existing rows.
 * [position] drives display order; [enabled] drives visibility - this is
 * the full "widget engine" data model the UI/UX spec's Widget System needs;
 * drag-to-reorder UI itself is a later polish pass, this phase wires
 * enable/disable + the stored order.
 */
@Entity(tableName = "dashboard_widget")
data class DashboardWidgetEntity(
    @PrimaryKey val widgetKey: String,
    val position: Int,
    val enabled: Boolean = true
)
