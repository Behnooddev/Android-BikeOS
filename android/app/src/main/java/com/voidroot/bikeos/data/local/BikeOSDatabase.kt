package com.voidroot.bikeos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.voidroot.bikeos.data.local.dao.AppStateDao
import com.voidroot.bikeos.data.local.dao.BikeProfileDao
import com.voidroot.bikeos.data.local.dao.DashboardWidgetDao
import com.voidroot.bikeos.data.local.dao.RideSessionDao
import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.dao.ThemeColorsDao
import com.voidroot.bikeos.data.local.dao.UserProfileDao
import com.voidroot.bikeos.data.local.entity.AppStateEntity
import com.voidroot.bikeos.data.local.entity.BikeProfileEntity
import com.voidroot.bikeos.data.local.entity.DashboardWidgetEntity
import com.voidroot.bikeos.data.local.entity.RideSessionEntity
import com.voidroot.bikeos.data.local.entity.SettingsEntity
import com.voidroot.bikeos.data.local.entity.ThemeColorsEntity
import com.voidroot.bikeos.data.local.entity.UserProfileEntity

/**
 * Schema version 2: added the onboarding/signup + password fields, app
 * lifecycle state, and per-day-mode theme colors introduced with the
 * onboarding/signup/settings rebuild.
 *
 * [MIGRATION_1_2] rebuilds `user_profile` from scratch (its columns
 * changed shape - name -> firstName/lastName, added password fields) and
 * adds the two new tables + settings columns, preserving bike/ride/widget
 * data. This migration is written carefully but NOT verified against a
 * running app (no Android runtime available while writing it) - if it
 * fails on your dev device, clearing app data is an acceptable fallback
 * pre-release; flag it back if it breaks so it can be fixed for real.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS user_profile")
        db.execSQL(
            """CREATE TABLE user_profile (
                id INTEGER NOT NULL PRIMARY KEY,
                username TEXT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                email TEXT NOT NULL,
                passwordHash TEXT NOT NULL,
                passwordSalt TEXT NOT NULL,
                age INTEGER NOT NULL,
                heightCm INTEGER NOT NULL,
                weightKg INTEGER NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS app_state (
                id INTEGER NOT NULL PRIMARY KEY,
                hasCompletedOnboarding INTEGER NOT NULL,
                hasCompletedSignup INTEGER NOT NULL,
                lastAppOpenEpochMs INTEGER NOT NULL,
                lastRideStartEpochDay INTEGER NOT NULL,
                avgRideStartMinuteOfDay INTEGER NOT NULL,
                rideStartSampleCount INTEGER NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS theme_colors (
                id INTEGER NOT NULL PRIMARY KEY,
                dayPrimary INTEGER NOT NULL,
                dayAccent INTEGER NOT NULL,
                dayBackground INTEGER NOT NULL,
                dayCardBackground INTEGER NOT NULL,
                dayTextPrimary INTEGER NOT NULL,
                nightPrimary INTEGER NOT NULL,
                nightAccent INTEGER NOT NULL,
                nightBackground INTEGER NOT NULL,
                nightCardBackground INTEGER NOT NULL,
                nightTextPrimary INTEGER NOT NULL
            )"""
        )

        db.execSQL("ALTER TABLE settings ADD COLUMN isDarkTheme INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE settings ADD COLUMN use24HourClock INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE settings ADD COLUMN gearSuggestionsEnabled INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE settings ADD COLUMN antiTheftAlarmEnabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE settings ADD COLUMN reminderNotificationsEnabled INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE settings ADD COLUMN engineStartAnimationEnabled INTEGER NOT NULL DEFAULT 1")
    }
}

/**
 * Schema version 3: Phase H added `avgAccelJerkG` to `ride_session` -
 * real MPU-based riding-style analysis (see RideSessionEntity kdoc).
 * Existing rows backfill to 0f (Room's ALTER TABLE default), which
 * HomeViewModel's ridingStyleFrom() already treats as "no real accel data
 * for this ride, fall back to the old speed-burstiness heuristic" - so
 * old rides keep working, they just don't get the new classifier
 * retroactively (impossible - the raw accel samples were never stored).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ride_session ADD COLUMN avgAccelJerkG REAL NOT NULL DEFAULT 0.0")
    }
}

/**
 * Phase K: adds the hardware-free mode toggle to the single-row settings
 * table. Defaults to false (hardware/ESP32 mode) for every existing user -
 * nobody gets silently switched into phone-sensor mode by an update.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE settings ADD COLUMN hardwareFreeModeEnabled INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        UserProfileEntity::class,
        BikeProfileEntity::class,
        RideSessionEntity::class,
        SettingsEntity::class,
        DashboardWidgetEntity::class,
        AppStateEntity::class,
        ThemeColorsEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class BikeOSDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bikeProfileDao(): BikeProfileDao
    abstract fun rideSessionDao(): RideSessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun dashboardWidgetDao(): DashboardWidgetDao
    abstract fun appStateDao(): AppStateDao
    abstract fun themeColorsDao(): ThemeColorsDao

    companion object {
        const val DATABASE_NAME = "bikeos.db"
    }
}
