package com.voidroot.bikeos.presentation.menu.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.backup.BackupManager
import com.voidroot.bikeos.data.ble.BikeOsDeviceInfo
import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.data.repository.AppStateRepository
import com.voidroot.bikeos.data.repository.BikeProfile
import com.voidroot.bikeos.data.repository.BikeRepository
import com.voidroot.bikeos.data.repository.BleRepository
import com.voidroot.bikeos.data.repository.DashboardConfigRepository
import com.voidroot.bikeos.data.repository.RideRepository
import com.voidroot.bikeos.data.repository.SettingsRepository
import com.voidroot.bikeos.data.repository.UserRepository
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
    private val bleRepository: BleRepository,
    private val userRepository: UserRepository,
    private val rideRepository: RideRepository,
    private val dashboardConfigRepository: DashboardConfigRepository,
    private val appStateRepository: AppStateRepository
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

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(isDarkTheme = isDark) } }
    }

    fun setUse24HourClock(use24Hour: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(use24HourClock = use24Hour) } }
    }

    fun setGearSuggestionsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(gearSuggestionsEnabled = enabled) } }
    }

    fun setAntiTheftAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(antiTheftAlarmEnabled = enabled) } }
        bleRepository.sendCommand(
            if (enabled) com.voidroot.bikeos.data.ble.ControlCommand.ARM_ALARM
            else com.voidroot.bikeos.data.ble.ControlCommand.DISARM_ALARM
        )
    }

    fun setReminderNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(reminderNotificationsEnabled = enabled) } }
    }

    fun setEngineStartAnimationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.update { it.copy(engineStartAnimationEnabled = enabled) } }
    }

    fun saveBikeConfig(bike: BikeProfile) {
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

    /**
     * Wipes every table BikeOS owns and calls [onDone] so the caller can
     * navigate back to Onboarding. Deliberately clears AppState LAST -
     * if anything above throws, the app should still look "set up" rather
     * than accidentally landing in a half-erased state that then also
     * thinks it needs onboarding again.
     */
    fun eraseAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            userRepository.clear()
            bikeRepository.clear()
            rideRepository.clearAll()
            settingsRepository.clear()
            dashboardConfigRepository.clear()
            appStateRepository.clear()
            onDone()
        }
    }
}
