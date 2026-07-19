package com.voidroot.bikeos.data.repository

import com.voidroot.bikeos.data.local.dao.AppStateDao
import com.voidroot.bikeos.data.local.entity.AppStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AppState(
    val hasCompletedOnboarding: Boolean = false,
    val hasCompletedSignup: Boolean = false,
    val lastAppOpenEpochMs: Long = 0L,
    val lastRideStartEpochDay: Long = -1L,
    val avgRideStartMinuteOfDay: Int = 480,
    val rideStartSampleCount: Int = 0
)

private fun AppStateEntity.toDomain() = AppState(
    hasCompletedOnboarding, hasCompletedSignup, lastAppOpenEpochMs,
    lastRideStartEpochDay, avgRideStartMinuteOfDay, rideStartSampleCount
)

private fun AppState.toEntity() = AppStateEntity(
    0, hasCompletedOnboarding, hasCompletedSignup, lastAppOpenEpochMs,
    lastRideStartEpochDay, avgRideStartMinuteOfDay, rideStartSampleCount
)

/**
 * App-lifecycle state: onboarding/signup completion, and the running
 * average of when the user starts rides (used to time the "haven't ridden
 * today" reminder notification - see ReminderScheduler).
 */
class AppStateRepository @Inject constructor(
    private val dao: AppStateDao
) {
    fun observe(): Flow<AppState> = dao.observe().map { it?.toDomain() ?: AppState() }

    suspend fun get(): AppState = dao.get()?.toDomain() ?: AppState()

    suspend fun markOnboardingComplete() {
        val current = dao.get() ?: AppStateEntity()
        dao.upsert(current.copy(hasCompletedOnboarding = true))
    }

    suspend fun markSignupComplete() {
        val current = dao.get() ?: AppStateEntity()
        dao.upsert(current.copy(hasCompletedSignup = true))
    }

    suspend fun recordAppOpen() {
        val current = dao.get() ?: AppStateEntity()
        dao.upsert(current.copy(lastAppOpenEpochMs = System.currentTimeMillis()))
    }

    /**
     * Incrementally folds a new "Start Ride" timestamp into the running
     * average minute-of-day (Welford-style running mean - no need to store
     * every historical start time just to compute an average).
     */
    suspend fun recordRideStart(epochMs: Long) {
        val current = dao.get() ?: AppStateEntity()
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
        val minuteOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
        val newCount = current.rideStartSampleCount + 1
        val newAvg = current.avgRideStartMinuteOfDay +
            (minuteOfDay - current.avgRideStartMinuteOfDay) / newCount

        val epochDay = TimeUnit.MILLISECONDS.toDays(epochMs)

        dao.upsert(
            current.copy(
                avgRideStartMinuteOfDay = newAvg,
                rideStartSampleCount = newCount,
                lastRideStartEpochDay = epochDay
            )
        )
    }

    /** Full reset - used by Settings > Erase Data, so onboarding/signup show again. */
    suspend fun clear() = dao.clear()
}
