package com.voidroot.bikeos.data.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class MusicState(
    val title: String? = null,
    val artist: String? = null,
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false
)

/**
 * Reads whatever music app currently has an active `MediaSession`
 * (Spotify, YouTube Music, etc.) via `MediaSessionManager` - this requires
 * "Notification access" (a special permission granted manually in system
 * settings, NOT a normal runtime permission dialog - see
 * [BikeOSNotificationListenerService] for why) because Android only lets
 * an app enumerate OTHER apps' active media sessions if it's an enabled
 * notification listener.
 *
 * Deliberately generic - doesn't integrate with any specific music app's
 * SDK, so it works with whatever the rider already has playing.
 */
@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val listenerComponent = ComponentName(context, BikeOSNotificationListenerService::class.java)

    private val _musicState = MutableStateFlow(MusicState())
    val musicState: StateFlow<MusicState> = _musicState.asStateFlow()

    private var activeController: MediaController? = null
    private var isListening = false

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateStateFrom(activeController)
        }
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateStateFrom(activeController)
        }
    }

    private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        attachToFirstController(controllers)
    }

    fun hasNotificationAccess(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        return enabled.contains(context.packageName)
    }

    fun startListening() {
        if (isListening || !hasNotificationAccess() || sessionManager == null) return
        isListening = true
        try {
            sessionManager.addOnActiveSessionsChangedListener(activeSessionsListener, listenerComponent)
            attachToFirstController(sessionManager.getActiveSessions(listenerComponent))
        } catch (e: SecurityException) {
            // Notification access was revoked between the check above and
            // this call (e.g. user just turned it off in Settings) -
            // fail closed rather than crash.
            isListening = false
        }
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        sessionManager?.removeOnActiveSessionsChangedListener(activeSessionsListener)
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
        _musicState.value = MusicState()
    }

    private fun attachToFirstController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(controllerCallback)
        activeController = controllers?.firstOrNull()
        activeController?.registerCallback(controllerCallback)
        updateStateFrom(activeController)
    }

    private fun updateStateFrom(controller: MediaController?) {
        val metadata = controller?.metadata
        val playbackState = controller?.playbackState
        _musicState.value = MusicState(
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
            isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        )
    }

    fun playPause() {
        val controller = activeController ?: return
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    fun skipNext() = activeController?.transportControls?.skipToNext()
    fun skipPrevious() = activeController?.transportControls?.skipToPrevious()
}
