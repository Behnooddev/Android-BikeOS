package com.voidroot.bikeos.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Low-level GATT wrapper around the Android BLE APIs - scanning, connecting,
 * service discovery, notification subscription, and control-command writes.
 *
 * Permission responsibility: every public method here assumes
 * BLUETOOTH_SCAN/BLUETOOTH_CONNECT (API 31+) or ACCESS_FINE_LOCATION
 * (API <31) has ALREADY been granted - this class does not request
 * permissions itself (that's a UI concern, see the Settings > Bluetooth
 * section). Calling these methods without permission throws a
 * SecurityException at the OS level, which is intentionally not
 * swallowed here so the failure is visible during development.
 *
 * Singleton scope: one physical BLE connection should exist for the whole
 * app, not per-screen.
 */
@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private var gatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null
    private var controlCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _sensorData = MutableSharedFlow<SensorPayload>(extraBufferCapacity = 8)
    val sensorData: SharedFlow<SensorPayload> = _sensorData.asSharedFlow()

    private val _buttonEvents = MutableSharedFlow<DeviceButtonEvent>(extraBufferCapacity = 8)
    val buttonEvents: SharedFlow<DeviceButtonEvent> = _buttonEvents.asSharedFlow()

    private val _deviceInfo = MutableStateFlow<BikeOsDeviceInfo?>(null)
    val deviceInfo: StateFlow<BikeOsDeviceInfo?> = _deviceInfo.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startScanAndConnect() {
        val scanner = adapter?.bluetoothLeScanner
        if (adapter?.isEnabled != true || scanner == null) {
            _connectionState.value = BleConnectionState.Failed("Bluetooth is off or unavailable")
            return
        }

        _connectionState.value = BleConnectionState.Scanning

        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(BleUuids.DEVICE_INFO_SERVICE)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                scanner.stopScan(this)
                connectToDevice(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                _connectionState.value = BleConnectionState.Failed("Scan failed (code $errorCode)")
            }
        }
        scanner.startScan(filters, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        adapter?.bluetoothLeScanner?.let { scanCallback?.let { cb -> it.stopScan(cb) } }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        _connectionState.value = BleConnectionState.Connecting
        gatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> g.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = BleConnectionState.Disconnected
                    controlCharacteristic = null
                    _deviceInfo.value = null
                    g.close()
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Failed("Service discovery failed")
                return
            }

            // Device Information Service - read once, per architecture review
            // (Device ID / Firmware / Protocol version are NOT in every packet).
            g.getService(BleUuids.DEVICE_INFO_SERVICE)?.let { service ->
                service.getCharacteristic(BleUuids.DEVICE_ID_CHARACTERISTIC)?.let { g.readCharacteristic(it) }
            }

            g.getService(BleUuids.CONTROL_SERVICE)?.let { service ->
                controlCharacteristic = service.getCharacteristic(BleUuids.CONTROL_COMMAND_CHARACTERISTIC)
            }

            g.getService(BleUuids.SENSOR_DATA_SERVICE)
                ?.getCharacteristic(BleUuids.SENSOR_DATA_CHARACTERISTIC)
                ?.let { characteristic ->
                    g.setCharacteristicNotification(characteristic, true)
                    val cccd = characteristic.getDescriptor(BleUuids.CLIENT_CHARACTERISTIC_CONFIG)
                    if (cccd != null) {
                        @Suppress("DEPRECATION")
                        cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        g.writeDescriptor(cccd)
                    }
                }

            _connectionState.value = BleConnectionState.Connected
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            if (characteristic.uuid == BleUuids.DEVICE_ID_CHARACTERISTIC) {
                @Suppress("DEPRECATION")
                val deviceId = characteristic.value?.decodeToString() ?: "unknown"
                _deviceInfo.value = BikeOsDeviceInfo(deviceId = deviceId)

                // chain the next handshake read - firmware version
                g.getService(BleUuids.DEVICE_INFO_SERVICE)
                    ?.getCharacteristic(BleUuids.FIRMWARE_VERSION_CHARACTERISTIC)
                    ?.let { g.readCharacteristic(it) }
            } else if (characteristic.uuid == BleUuids.FIRMWARE_VERSION_CHARACTERISTIC) {
                @Suppress("DEPRECATION")
                val firmwareVersion = characteristic.value?.decodeToString() ?: "unknown"
                _deviceInfo.value = _deviceInfo.value?.copy(firmwareVersion = firmwareVersion)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid != BleUuids.SENSOR_DATA_CHARACTERISTIC) return
            val value = characteristic.value ?: return
            when (val decoded = BlePacket.decode(value)) {
                is BlePacket.DecodedNotification.SensorData -> _sensorData.tryEmit(decoded.payload)
                is BlePacket.DecodedNotification.ButtonEvent -> _buttonEvents.tryEmit(decoded.event)
                null -> Unit // malformed/unrecognized - already logged as dropped implicitly by decode() returning null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendControlCommand(command: ControlCommand, param: Int? = null) {
        val char = controlCharacteristic ?: return
        @Suppress("DEPRECATION")
        char.value = BlePacket.encodeControlCommand(command, param)
        @Suppress("DEPRECATION")
        gatt?.writeCharacteristic(char)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
        gatt = null
        _connectionState.value = BleConnectionState.Disconnected
    }
}

data class BikeOsDeviceInfo(
    val deviceId: String,
    val firmwareVersion: String? = null
)
