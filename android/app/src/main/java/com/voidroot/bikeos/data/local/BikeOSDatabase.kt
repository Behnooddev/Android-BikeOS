package com.voidroot.bikeos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voidroot.bikeos.data.local.dao.BikeProfileDao
import com.voidroot.bikeos.data.local.dao.DashboardWidgetDao
import com.voidroot.bikeos.data.local.dao.RideSessionDao
import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.dao.UserProfileDao
import com.voidroot.bikeos.data.local.entity.BikeProfileEntity
import com.voidroot.bikeos.data.local.entity.DashboardWidgetEntity
import com.voidroot.bikeos.data.local.entity.RideSessionEntity
import com.voidroot.bikeos.data.local.entity.SettingsEntity
import com.voidroot.bikeos.data.local.entity.UserProfileEntity

/**
 * Schema version 1 (Phase 2 - first Room schema BikeOS ships with).
 *
 * Migration policy (per Update/Security spec - "never delete user data
 * during updates"): every future version bump must add a Migration object
 * to the list passed into Room.databaseBuilder, and destructive fallback
 * must NEVER be enabled in release builds. `exportSchema = true` writes the
 * schema JSON under app/schemas/ so future migrations can be tested against
 * a real previous-version schema, not written blind.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        BikeProfileEntity::class,
        RideSessionEntity::class,
        SettingsEntity::class,
        DashboardWidgetEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BikeOSDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bikeProfileDao(): BikeProfileDao
    abstract fun rideSessionDao(): RideSessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun dashboardWidgetDao(): DashboardWidgetDao

    companion object {
        const val DATABASE_NAME = "bikeos.db"
    }
}
