package com.voidroot.bikeos.presentation.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.ble.ControlCommand
import com.voidroot.bikeos.data.repository.BleRepository
import com.voidroot.bikeos.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-wide anti-theft alarm guard. Lives above the NavGraph (see
 * AlarmGuard.kt) so the disarm dialog can appear no matter which screen is
 * showing when the ESP32 reports a trigger - the whole point is speed at
 * 2am, not "go find the right screen first".
 */
@HiltViewModel
class AlarmGuardViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val isTriggered: StateFlow<Boolean> = bleRepository.alarmTriggered

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun attemptDisarm(password: String) {
        viewModelScope.launch {
            if (userRepository.verifyPassword(password)) {
                bleRepository.sendCommand(ControlCommand.DISARM_ALARM)
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Incorrect password"
            }
        }
    }
}
