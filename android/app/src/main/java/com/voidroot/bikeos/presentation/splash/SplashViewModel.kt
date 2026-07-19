package com.voidroot.bikeos.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.core.navigation.BikeOSDestinations
import com.voidroot.bikeos.data.repository.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _nextRoute = MutableStateFlow<String?>(null)
    val nextRoute: StateFlow<String?> = _nextRoute.asStateFlow()

    init {
        viewModelScope.launch {
            appStateRepository.recordAppOpen() // also re-enables the reminder notification if it had gone quiet
            val state = appStateRepository.get()
            _nextRoute.value = when {
                !state.hasCompletedOnboarding -> BikeOSDestinations.ONBOARDING
                !state.hasCompletedSignup -> BikeOSDestinations.SIGNUP
                else -> BikeOSDestinations.MENU_HOME
            }
        }
    }
}
