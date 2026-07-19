package com.voidroot.bikeos.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Drives the app-wide Material dark/light choice - separate from the cluster's own day/night color customization (see ClusterPalette). */
@HiltViewModel
class AppThemeViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val isDarkTheme: StateFlow<Boolean> = settingsRepository.observe()
        .map { it.isDarkTheme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
