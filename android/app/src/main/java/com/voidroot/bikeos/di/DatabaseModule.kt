package com.voidroot.bikeos.di

import android.content.Context
import androidx.room.Room
import com.voidroot.bikeos.data.local.BikeOSDatabase
import com.voidroot.bikeos.data.local.dao.BikeProfileDao
import com.voidroot.bikeos.data.local.dao.DashboardWidgetDao
import com.voidroot.bikeos.data.local.dao.RideSessionDao
import com.voidroot.bikeos.data.local.dao.SettingsDao
import com.voidroot.bikeos.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Single Room instance for the whole app lifetime.
 * `fallbackToDestructiveMigration()` is deliberately NOT enabled - per the
 * Update/Security spec, user data must never be silently dropped on a
 * schema bump. Every future version increase needs a real Migration added
 * to `.addMigrations(...)` here instead.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BikeOSDatabase =
        Room.databaseBuilder(context, BikeOSDatabase::class.java, BikeOSDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideUserProfileDao(db: BikeOSDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideBikeProfileDao(db: BikeOSDatabase): BikeProfileDao = db.bikeProfileDao()

    @Provides
    fun provideRideSessionDao(db: BikeOSDatabase): RideSessionDao = db.rideSessionDao()

    @Provides
    fun provideSettingsDao(db: BikeOSDatabase): SettingsDao = db.settingsDao()

    @Provides
    fun provideDashboardWidgetDao(db: BikeOSDatabase): DashboardWidgetDao = db.dashboardWidgetDao()
}
