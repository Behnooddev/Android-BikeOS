package com.voidroot.bikeos.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Simple salted SHA-256 hashing for the local signup password.
 *
 * This is NOT bcrypt/Argon2/scrypt - those are the right choice for a
 * server-side auth system, but adding a native/JNI KDF library just for a
 * local, no-server password (used today only as the anti-theft alarm's
 * disarm code) is disproportionate. If/when a real server-backed account
 * system arrives, re-hash server-side with a proper KDF at that point -
 * don't assume this hash format carries over.
 */
object PasswordHasher {
    private const val ITERATIONS = 100_000 // repeated SHA-256 - cheap PBKDF-ish stretching without a new dependency

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, salt: String): String {
        var digest = MessageDigest.getInstance("SHA-256")
        var result = (password + salt).toByteArray(Charsets.UTF_8)
        repeat(ITERATIONS) {
            digest = MessageDigest.getInstance("SHA-256")
            result = digest.digest(result)
        }
        return Base64.getEncoder().encodeToString(result)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}
