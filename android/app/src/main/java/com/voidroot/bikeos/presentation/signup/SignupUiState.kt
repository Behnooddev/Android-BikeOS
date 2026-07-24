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
    // Bike Configuration - collected in the same flow now, per the "do
    // bike setup at signup, not a separate trip to Settings later" ask.
    val bikeName: String = "My Bike",
    val bikeType: String = "Mountain Bike",
    val wheelSizeInches: String = "27.5",
    val frontGearCount: String = "1",
    val rearGearCount: String = "1",
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)
