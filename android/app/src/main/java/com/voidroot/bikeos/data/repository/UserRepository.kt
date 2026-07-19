package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.UserProfileDao
import com.voidroot.bikeos.data.local.entity.UserProfileEntity
import com.voidroot.bikeos.data.security.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Domain-facing model. Kept separate from [UserProfileEntity] so screens
 * never import a Room annotation. Password hash/salt are intentionally NOT
 * exposed here - screens work with plaintext only transiently during
 * signup/verification (see [UserRepository.signUp]/[verifyPassword]),
 * never read a stored hash back out.
 */
data class UserProfile(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val age: Int = 0,
    val heightCm: Int = 0,
    val weightKg: Int = 0
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

private fun UserProfileEntity.toDomain() = UserProfile(username, firstName, lastName, email, age, heightCm, weightKg)

class UserRepository @Inject constructor(
    private val dao: UserProfileDao
) {
    fun observe(): Flow<UserProfile> = dao.observe().map { it?.toDomain() ?: UserProfile() }

    /**
     * Saves profile fields WITHOUT touching the stored password - used by
     * the Profile edit screen, which never sees/edits the password hash.
     */
    suspend fun save(profile: UserProfile) {
        val existing = dao.get() ?: UserProfileEntity()
        dao.upsert(
            existing.copy(
                username = profile.username,
                firstName = profile.firstName,
                lastName = profile.lastName,
                email = profile.email,
                age = profile.age,
                heightCm = profile.heightCm,
                weightKg = profile.weightKg
            )
        )
    }

    /** Signup: hashes+salts the password fresh and stores the full profile. */
    suspend fun signUp(profile: UserProfile, password: String) {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        dao.upsert(
            UserProfileEntity(
                id = 0,
                username = profile.username,
                firstName = profile.firstName,
                lastName = profile.lastName,
                email = profile.email,
                passwordHash = hash,
                passwordSalt = salt,
                age = profile.age,
                heightCm = profile.heightCm,
                weightKg = profile.weightKg
            )
        )
    }

    /** Used both by a future login flow and the anti-theft alarm's quick-disarm dialog. */
    suspend fun verifyPassword(password: String): Boolean {
        val entity = dao.get() ?: return false
        if (entity.passwordHash.isEmpty()) return false // no account set up yet
        return PasswordHasher.verify(password, entity.passwordSalt, entity.passwordHash)
    }

    suspend fun hasAccount(): Boolean = dao.get()?.passwordHash?.isNotEmpty() == true

    suspend fun clear() = dao.clear()
}
