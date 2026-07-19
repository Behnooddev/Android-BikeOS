package com.voidroot.bikeos.presentation.signup

data class SignupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)
