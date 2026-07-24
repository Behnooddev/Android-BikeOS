package com.voidroot.bikeos.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.voidroot.bikeos.R

private const val CHANNEL_ID = "bikeos_reminders"
private const val NOTIFICATION_ID = 1001

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ride reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you to ride if you haven't yet today, around your usual time."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * Caller is responsible for checking POST_NOTIFICATIONS is granted
     * (API 33+) before calling this - NotificationManagerCompat.notify()
     * silently no-ops without it, but we don't want to rely on that
     * silently swallowing the call; ReminderWorker checks explicitly.
     */
    fun showRideReminder(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Haven't ridden today?")
            .setContentText("It's about your usual time - your bike is waiting.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
