package com.voidroot.bikeos.presentation.menu.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.DashboardConfigRepository
import com.voidroot.bikeos.data.repository.DashboardWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val dashboardConfigRepository: DashboardConfigRepository
) : ViewModel() {

    val widgets: StateFlow<List<DashboardWidget>> = dashboardConfigRepository.observeWidgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setEnabled(key: String, enabled: Boolean) {
        viewModelScope.launch { dashboardConfigRepository.setEnabled(key, enabled) }
    }
}
