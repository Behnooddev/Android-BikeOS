package com.voidroot.bikeos.data.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules [ReminderWorker] to run every 30 minutes (WorkManager's
 * minimum periodic interval is 15 minutes; 30 is plenty for a +/-30-minute
 * "around your usual time" check - no need to burn battery checking more
 * often). Most runs are a no-op inside the worker itself; this class just
 * makes sure exactly one periodic job exists, ever (KEEP policy - calling
 * this again on every app launch does not stack duplicate jobs).
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleIfNeeded() {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            ReminderWorker.INTERVAL_MINUTES, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
