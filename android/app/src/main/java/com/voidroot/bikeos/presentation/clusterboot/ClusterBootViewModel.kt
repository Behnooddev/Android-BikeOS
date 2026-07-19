package com.voidroot.bikeos.presentation.clusterboot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.ble.BleConnectionState
import com.voidroot.bikeos.data.repository.BleRepository
import com.voidroot.bikeos.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ClusterBootViewModel @Inject constructor(
    private val bleRepository: BleRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val connectionState: StateFlow<BleConnectionState> = bleRepository.connectionState

    val engineAnimationEnabled: StateFlow<Boolean> = settingsRepository.observe()
        .map { it.engineStartAnimationEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun connect() {
        if (connectionState.value !is BleConnectionState.Connected) {
            bleRepository.connect()
        }
    }
}
