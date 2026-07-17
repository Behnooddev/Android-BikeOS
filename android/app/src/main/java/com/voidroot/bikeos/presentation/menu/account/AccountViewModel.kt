package com.voidroot.bikeos.presentation.menu.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.UserProfile
import com.voidroot.bikeos.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        // true one-shot load: this screen edits a local copy rather than
        // re-rendering on every Room emission (which would fight typing)
        viewModelScope.launch { _profile.value = userRepository.observe().first() }
    }

    fun update(transform: (UserProfile) -> UserProfile) {
        _profile.value = transform(_profile.value)
        _saved.value = false
    }

    fun save() {
        viewModelScope.launch {
            userRepository.save(_profile.value)
            _saved.value = true
        }
    }
}
