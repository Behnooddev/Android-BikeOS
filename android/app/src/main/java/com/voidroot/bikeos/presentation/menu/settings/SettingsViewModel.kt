package com.voidroot.bikeos.presentation.menu.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.backup.BackupManager
import com.voidroot.bikeos.data.ble.BikeOsDeviceInfo
import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.data.repository.BikeRepository
import com.voidroot.bikeos.data.repository.BleRepository
import com.voidroot.bikeos.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val bikeRepository: BikeRepository,
    private val backupManager: BackupManager,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _backupMessage = MutableStateFlow<String?>(null)

    val connectionState: StateFlow<BleConnectionState> = bleRepository.connectionState
    val deviceInfo: StateFlow<BikeOsDeviceInfo?> = bleRepository.deviceInfo

    fun connectDevice() = bleRepository.connect()
    fun disconnectDevice() = bleRepository.disconnect()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observe(),
        bikeRepository.observe(),
        _backupMessage
    ) { settings, bike, message ->
        SettingsUiState(settings, bike, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setMetricUnits(useMetric: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(useMetricUnits = useMetric) } }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(soundEnabled = enabled) } }
    }

    fun setMaxSpeedAlert(kmh: Int) {
        viewModelScope.launch { settingsRepository.update { it.copy(maxSpeedAlertKmh = kmh) } }
    }

    fun saveBikeConfig(bike: com.voidroot.bikeos.data.repository.BikeProfile) {
        viewModelScope.launch { bikeRepository.save(bike) }
    }

    fun syncGear(front: Int, rear: Int) {
        viewModelScope.launch { bikeRepository.syncCurrentGear(front, rear) }
    }

    fun exportBackup() {
        viewModelScope.launch {
            val result = runCatching { backupManager.export() }
            _backupMessage.value = result.fold(
                onSuccess = { "Backup exported to ${it.name}" },
                onFailure = { "Export failed: ${it.message}" }
            )
        }
    }

    fun importBackup() {
        viewModelScope.launch {
            val result = backupManager.import()
            _backupMessage.value = result.fold(
                onSuccess = { "Backup restored successfully" },
                onFailure = { "Import failed: ${it.message}" }
            )
        }
    }
}
