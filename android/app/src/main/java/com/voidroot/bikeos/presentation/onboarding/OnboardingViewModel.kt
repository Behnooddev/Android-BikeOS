package com.voidroot.bikeos.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository
) : ViewModel() {
    fun onFinished(onDone: () -> Unit) {
        viewModelScope.launch {
            appStateRepository.markOnboardingComplete()
            onDone()
        }
    }
}
