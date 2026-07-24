package com.voidroot.bikeos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.voidroot.bikeos.data.notification.NotificationHelper
import com.voidroot.bikeos.data.notification.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * BikeOS Application entry point.
 *
 * Hilt is wired (@HiltAndroidApp generates the DI container root).
 * Repositories/DAOs/Database are provided via
 * [com.voidroot.bikeos.di.DatabaseModule].
 *
 * Implements WorkManager's Configuration.Provider so [ReminderWorker] (a
 * @HiltWorker) can be constructor-injected instead of needing a no-arg
 * constructor - the standard way to make Hilt and WorkManager cooperate.
 */
@HiltAndroidApp
class BikeOSApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        reminderScheduler.scheduleIfNeeded()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
