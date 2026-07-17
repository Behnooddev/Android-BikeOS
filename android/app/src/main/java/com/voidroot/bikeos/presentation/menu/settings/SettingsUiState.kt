package com.voidroot.bikeos.presentation.menu.settings

import com.voidroot.bikeos.data.repository.AppSettings
import com.voidroot.bikeos.data.repository.BikeProfile

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val bike: BikeProfile = BikeProfile(),
    val backupMessage: String? = null
)
