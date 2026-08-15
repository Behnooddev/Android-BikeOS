package com.voidroot.bikeos.presentation.common

import android.util.Log
import android.view.KeyEvent
import com.voidroot.bikeos.data.ble.ControlCommand
import com.voidroot.bikeos.data.repository.BleRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase H - keyless starter.
 *
 * The builder's remote is a generic Bluetooth camera-shutter remote (the
 * kind sold for phone selfies/monopods) - it's a Bluetooth HID device,
 * not a custom BLE peripheral, so it pairs to the PHONE the normal
 * Android Bluetooth-accessory way (same as any BT keyboard), exactly
 * like it would for its original camera-shutter purpose. It does NOT
 * connect to the ESP32 directly - seeing this file's kdoc? See
 * `docs/23_PHASE_H_KEYLESS_STARTER.md` for the full reasoning on why
 * that approach (ESP32 as a BLE HID host) was rejected in favor of this
 * one (phone intercepts the HID keypresses, forwards a command over the
 * BLE link the app already has to the ESP32).
 *
 * IMPORTANT - UNVERIFIED MAPPING: this was written without the physical
 * remote in hand, so [keyCodeMap] below is a first-guess based on common
 * cheap BT shutter remotes, not a confirmed mapping. Every keyCode NOT in
 * the map gets logged (see the `else` branch in [onKeyEvent]) specifically
 * so the builder can press each of the remote's 3 buttons once, grep
 * Logcat for "RemoteKeyHandler: unmapped keyCode", and correct the map
 * below to match - no code restructuring needed for that, just edit the
 * map's entries.
 */
@Singleton
class RemoteKeyHandler @Inject constructor(
    private val bleRepository: BleRepository
) {
    companion object {
        private const val TAG = "RemoteKeyHandler"
    }

    /**
     * keyCode -> command sent once per press (ACTION_DOWN only, repeat
     * events from a held button are ignored - see [onKeyEvent]).
     *
     * Per the builder's spec: one button = ARM, one = DISARM, one (the
     * main/shutter button) = SYSTEM_ON ("turn everything on" - lights +
     * 3-beep chime, see bikeos_protocol.h's BIKEOS_CMD_SYSTEM_ON kdoc).
     *
     * First-guess keyCodes (UNVERIFIED - see class kdoc): most cheap BT
     * shutter remotes' main button sends KEYCODE_CAMERA or
     * KEYCODE_VOLUME_DOWN; a secondary zoom rocker commonly sends
     * KEYCODE_VOLUME_UP/DOWN too. If Logcat shows the SAME keyCode firing
     * for more than one physical button on the real remote, this simple
     * 1:1 table won't work as-is and needs a different disambiguation
     * (e.g. press-count or timing based) - cross that bridge once real
     * Logcat output is available, don't guess further without it.
     */
    private val keyCodeMap: Map<Int, ControlCommand> = mapOf(
        KeyEvent.KEYCODE_CAMERA to ControlCommand.SYSTEM_ON,
        KeyEvent.KEYCODE_VOLUME_DOWN to ControlCommand.SYSTEM_ON,
        KeyEvent.KEYCODE_VOLUME_UP to ControlCommand.ARM_ALARM,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to ControlCommand.DISARM_ALARM
    )

    /**
     * Call from the hosting Activity's `onKeyDown(keyCode, event)`.
     * Returns true if the keyCode was recognized and a command was sent -
     * the caller should treat that as "consumed" (return true from
     * onKeyDown too) so e.g. VOLUME_DOWN doesn't ALSO change the phone's
     * media volume at the same time it triggers SYSTEM_ON.
     */
    fun onKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount != 0) return false // ignore repeats from a held-down button

        val command = keyCodeMap[keyCode]
        if (command == null) {
            Log.d(TAG, "unmapped keyCode=$keyCode - press each remote button once and check this log to build the real mapping")
            return false
        }

        Log.d(TAG, "keyCode=$keyCode -> $command")
        bleRepository.sendCommand(command)
        return true
    }
}
