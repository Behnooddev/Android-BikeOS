package com.voidroot.bikeos.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.AppStateRepository
import com.voidroot.bikeos.data.repository.UserProfile
import com.voidroot.bikeos.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun update(transform: (SignupUiState) -> SignupUiState) {
        _uiState.value = transform(_uiState.value).copy(errorMessage = null)
    }

    /**
     * Validates then persists the account. On success calls [onDone] so the
     * screen can navigate - kept as a callback rather than a nav event flow
     * to keep this ViewModel simple; this screen only ever has one exit path.
     */
    fun submit(onDone: () -> Unit) {
        val state = _uiState.value

        val error = validate(state)
        if (error != null) {
            _uiState.value = state.copy(errorMessage = error)
            return
        }

        _uiState.value = state.copy(isSubmitting = true)
        viewModelScope.launch {
            userRepository.signUp(
                profile = UserProfile(
                    username = state.username,
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    age = state.age.toIntOrNull() ?: 0,
                    heightCm = state.heightCm.toIntOrNull() ?: 0,
                    weightKg = state.weightKg.toIntOrNull() ?: 0
                ),
                password = state.password
            )
            appStateRepository.markSignupComplete()
            _uiState.value = _uiState.value.copy(isSubmitting = false)
            onDone()
        }
    }

    private fun validate(state: SignupUiState): String? = when {
        state.firstName.isBlank() -> "First name is required"
        state.lastName.isBlank() -> "Last name is required"
        state.username.isBlank() -> "Username is required"
        state.email.isBlank() || !state.email.contains("@") -> "Enter a valid email"
        state.password.length < 6 -> "Password must be at least 6 characters"
        state.password != state.confirmPassword -> "Passwords do not match"
        state.age.toIntOrNull() == null || state.age.toInt() <= 0 -> "Enter a valid age"
        state.heightCm.toIntOrNull() == null || state.heightCm.toInt() <= 0 -> "Enter a valid height"
        state.weightKg.toIntOrNull() == null || state.weightKg.toInt() <= 0 -> "Enter a valid weight"
        else -> null
    }
}
