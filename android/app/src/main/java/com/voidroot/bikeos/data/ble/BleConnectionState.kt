package com.voidroot.bikeos.data.ble

/**
 * Connection lifecycle exposed to the UI. Deliberately a sealed class
 * (not a Boolean) so "why disconnected" is representable - the UI/UX spec
 * requires showing connection state clearly, not just connected/not.
 */
sealed class BleConnectionState {
    data object Disconnected : BleConnectionState()
    data object Scanning : BleConnectionState()
    data object Connecting : BleConnectionState()
    data object Connected : BleConnectionState()
    data class Failed(val reason: String) : BleConnectionState()
}
