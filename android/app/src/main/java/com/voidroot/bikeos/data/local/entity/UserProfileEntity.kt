package com.voidroot.bikeos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table - BikeOS is a local-account, single-user product for now
 * (per Update/Security spec: "Current version: Local account system").
 * [id] is always 0. Multi-user support is not planned; if it's ever needed
 * this becomes a real auto-generated PK instead of a schema rewrite.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val heightCm: Int = 0,
    val weightKg: Int = 0
)
