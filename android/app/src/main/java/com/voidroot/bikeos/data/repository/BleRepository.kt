package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.ble.BikeOsDeviceInfo
import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.data.ble.BleManager
import com.voidroot.bikeos.data.ble.ControlCommand
import com.voidroot.bikeos.data.ble.DeviceButtonEvent
import com.voidroot.bikeos.data.ble.SensorPayload
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin repository facade over [BleManager] - kept separate so screens
 * depend on a repository (consistent with every other data source in the
 * app) instead of reaching into the raw GATT wrapper directly.
 */
class BleRepository @Inject constructor(
    private val bleManager: BleManager
) {
    val connectionState: StateFlow<BleConnectionState> = bleManager.connectionState
    val deviceInfo: StateFlow<BikeOsDeviceInfo?> = bleManager.deviceInfo
    val sensorData: SharedFlow<SensorPayload> = bleManager.sensorData
    val buttonEvents: SharedFlow<DeviceButtonEvent> = bleManager.buttonEvents
    val alarmTriggered: StateFlow<Boolean> = bleManager.alarmTriggered

    fun connect() = bleManager.startScanAndConnect()
    fun disconnect() = bleManager.disconnect()
    fun sendCommand(command: ControlCommand, param: Int? = null) = bleManager.sendControlCommand(command, param)
}
