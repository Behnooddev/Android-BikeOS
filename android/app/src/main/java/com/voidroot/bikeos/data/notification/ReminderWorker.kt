package com.voidroot.bikeos.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voidroot.bikeos.data.repository.AppStateRepository
import com.voidroot.bikeos.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Runs periodically (every 30 min, see ReminderScheduler) and decides
 * whether to actually show a notification THIS run - most runs are a
 * no-op. Fires only when ALL of these hold:
 *  1. Settings > "Reminder notifications" is on.
 *  2. The app has been opened within the last 15 days (per the spec: if
 *     it's been longer than that, stay quiet until the user opens the app
 *     again on their own - don't nag someone who's stopped using it).
 *  3. The user hasn't already started a ride today.
 *  4. The current time is within +/-30 minutes of their learned average
 *     ride-start time (AppState.avgRideStartMinuteOfDay).
 *  5. POST_NOTIFICATIONS is granted (API 33+ only - not required below that).
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val appStateRepository: AppStateRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "bikeos_reminder_check"
        const val INTERVAL_MINUTES = 30L
        private const val FIFTEEN_DAYS_MS = 15L * 24 * 60 * 60 * 1000
        private const val WINDOW_MINUTES = 30
    }

    override suspend fun doWork(): Result {
        val settings = kotlinx.coroutines.flow.first(settingsRepository.observe())
        val appState = appStateRepository.get()

        if (!settings.reminderNotificationsEnabled) return Result.success()

        val now = System.currentTimeMillis()
        if (appState.lastAppOpenEpochMs == 0L || now - appState.lastAppOpenEpochMs > FIFTEEN_DAYS_MS) {
            return Result.success() // quiet until the app is opened again
        }

        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(now)
        if (appState.lastRideStartEpochDay == todayEpochDay) {
            return Result.success() // already rode today
        }

        val nowMinuteOfDay = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
        if (Math.abs(nowMinuteOfDay - appState.avgRideStartMinuteOfDay) > WINDOW_MINUTES) {
            return Result.success() // not the right time of day yet
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success()
        }

        NotificationHelper.showRideReminder(applicationContext)
        return Result.success()
    }
}
