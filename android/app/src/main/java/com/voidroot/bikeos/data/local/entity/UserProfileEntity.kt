package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table - BikeOS is a local-account, single-user product for now
 * (per Update/Security spec: "Current version: Local account system").
 * [id] is always 0.
 *
 * [passwordHash]/[passwordSalt]: the signup password isn't used for a real
 * login flow (there's no server yet) - it exists now so (a) it's ready for
 * a future account/server feature, and (b) it doubles as the anti-theft
 * alarm's disarm code. NEVER store the plaintext password - see
 * PasswordHasher for the hashing/salting logic.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val age: Int = 0,
    val heightCm: Int = 0,
    val weightKg: Int = 0
)
