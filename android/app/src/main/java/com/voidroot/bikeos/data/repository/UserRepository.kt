package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.UserProfileDao
import com.voidroot.bikeos.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Domain-facing model. Kept separate from [UserProfileEntity] so screens
 * never import a Room annotation - if the schema changes shape later,
 * only the mapping in this file needs to change, not every call site.
 */
data class UserProfile(
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val heightCm: Int = 0,
    val weightKg: Int = 0
)

private fun UserProfileEntity.toDomain() = UserProfile(username, name, email, age, heightCm, weightKg)
private fun UserProfile.toEntity() = UserProfileEntity(0, username, name, email, age, heightCm, weightKg)

class UserRepository @Inject constructor(
    private val dao: UserProfileDao
) {
    fun observe(): Flow<UserProfile> = dao.observe().map { it?.toDomain() ?: UserProfile() }

    suspend fun save(profile: UserProfile) = dao.upsert(profile.toEntity())
}
