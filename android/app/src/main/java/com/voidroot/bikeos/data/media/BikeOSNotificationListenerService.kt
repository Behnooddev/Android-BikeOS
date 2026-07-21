package com.voidroot.bikeos.data.media

import android.service.notification.NotificationListenerService

/**
 * Deliberately does nothing with notifications themselves - its only
 * purpose is to exist as a bound, granted NotificationListenerService, so
 * [MusicRepository] is authorized to call
 * `MediaSessionManager.getActiveSessions(ComponentName(this, ...))` and
 * read whatever media session the phone's music app (Spotify, YouTube
 * Music, etc.) currently has active. This is why the Music widget needs
 * the special "Notification access" permission, not a normal runtime
 * permission - see MusicRepository's kdoc.
 */
class BikeOSNotificationListenerService : NotificationListenerService()
