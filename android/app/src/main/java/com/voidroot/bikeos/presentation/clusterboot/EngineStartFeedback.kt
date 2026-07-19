package com.voidroot.bikeos.presentation.clusterboot

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.delay

/**
 * The "engine catching" moment: one short, plain tone (a synthesized
 * buzzer-style beep via ToneGenerator - no audio asset needed, and
 * deliberately not a cutesy jingle) plus a short haptic pulse, timed
 * together. Requires only the normal VIBRATE permission (no runtime prompt).
 */
suspend fun playEngineStartFeedback(context: Context) {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
    triggerHaptic(context)
    delay(250)
    toneGenerator.release()
}

private fun triggerHaptic(context: Context) {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
}
