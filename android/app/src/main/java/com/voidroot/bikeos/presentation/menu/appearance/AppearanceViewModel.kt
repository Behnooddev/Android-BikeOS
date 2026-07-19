package com.voidroot.bikeos.presentation.menu.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.DashboardConfigRepository
import com.voidroot.bikeos.data.repository.DashboardWidget
import com.voidroot.bikeos.data.repository.ThemeColors
import com.voidroot.bikeos.data.repository.ThemeColorsRepository
import com.voidroot.bikeos.data.repository.ThemePalette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val dashboardConfigRepository: DashboardConfigRepository,
    private val themeColorsRepository: ThemeColorsRepository
) : ViewModel() {

    val widgets: StateFlow<List<DashboardWidget>> = dashboardConfigRepository.observeWidgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeColors: StateFlow<ThemeColors> = themeColorsRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeColors(
            day = ThemePalette(0xFF0077CC, 0xFF7C4DFF, 0xFFF5F7FA, 0x33000000, 0xFF0A0E14),
            night = ThemePalette(0xFF00E5FF, 0xFF7C4DFF, 0xFF0A0E14, 0x33FFFFFF, 0xFFF5F7FA)
        ))

    fun setEnabled(key: String, enabled: Boolean) {
        viewModelScope.launch { dashboardConfigRepository.setEnabled(key, enabled) }
    }

    fun setColor(mode: DayNightMode, role: ColorRole, colorArgb: Long) {
        viewModelScope.launch {
            val current = themeColors.value
            val updated = when (mode) {
                DayNightMode.DAY -> current.copy(day = current.day.withRole(role, colorArgb))
                DayNightMode.NIGHT -> current.copy(night = current.night.withRole(role, colorArgb))
            }
            themeColorsRepository.save(updated)
        }
    }

    private fun ThemePalette.withRole(role: ColorRole, colorArgb: Long): ThemePalette = when (role) {
        ColorRole.PRIMARY -> copy(primary = colorArgb)
        ColorRole.ACCENT -> copy(accent = colorArgb)
        ColorRole.BACKGROUND -> copy(background = colorArgb)
        ColorRole.CARD_BACKGROUND -> copy(cardBackground = colorArgb)
        ColorRole.TEXT_PRIMARY -> copy(textPrimary = colorArgb)
    }
}
