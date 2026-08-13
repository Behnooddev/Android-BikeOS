package com.voidroot.bikeos.presentation.menu.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voidroot.bikeos.data.repository.RideRepository
import com.voidroot.bikeos.data.repository.RideSession
import com.voidroot.bikeos.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Greeting copy + the two recent-info glass cards on Home.
 *
 * The greeting template is picked ONCE per ViewModel instance (roughly:
 * once per Home screen visit), not re-rolled on every data emission -
 * otherwise it would flicker to a different sentence every time a ride
 * finishes or the profile changes while this screen is visible.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    userRepository: UserRepository,
    rideRepository: RideRepository
) : ViewModel() {

    private val nameTemplates = listOf(
        "%s, where are we going today?",
        "Welcome back, %s!",
        "%s, ready for a ride?",
        "Hey %s - your bike is waiting."
    )
    private val distanceTemplates = listOf(
        "Did you know you've ridden %.1f km with BikeOS so far?",
        "You've covered %.1f km so far - keep it up!"
    )
    private val chosenTemplate = (nameTemplates + distanceTemplates).random()

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.observe(),
        rideRepository.observeRecent(50)
    ) { user, rides ->
        val totalDistance = rides.sumOf { it.distanceKm.toDouble() }.toFloat()
        val greeting = if (chosenTemplate.contains("%.1f")) {
            String.format(chosenTemplate, totalDistance)
        } else {
            String.format(chosenTemplate, user.firstName.ifBlank { "Rider" })
        }

        HomeUiState(
            firstName = user.firstName,
            greetingMessage = greeting,
            totalDistanceKm = totalDistance,
            ridingStyleSummary = ridingStyleFrom(rides)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /**
     * Prefers the real MPU-based classifier (protocol 1.2 / Phase H) -
     * only rides recorded with a real, connected accel stream have a
     * nonzero avgAccelJerkG (see RideSessionEntity's kdoc: 0f means "no
     * real accel data for this ride", not "rode perfectly smoothly", so
     * it's a safe filter condition). Falls back to the older max/avg-
     * speed-ratio heuristic for rides recorded before this existed, or
     * when there simply isn't yet enough real accel data.
     */
    private fun ridingStyleFrom(rides: List<RideSession>): String {
        val ridesWithAccelData = rides.filter { it.avgAccelJerkG > 0f }

        return when {
            ridesWithAccelData.size >= 3 -> ridingStyleFromAccel(ridesWithAccelData)
            rides.size >= 3 -> ridingStyleFromSpeedBurstiness(rides)
            else -> "Not enough data yet - go for a ride!"
        }
    }

    /**
     * Real classifier: average "jerk" (frame-to-frame accel-magnitude
     * change - see [com.voidroot.bikeos.presentation.dashboard.DashboardViewModel]'s
     * RideAccumulator kdoc for the exact definition) across recent rides
     * with real MPU data.
     *
     * Thresholds below are a first-pass estimate, NOT calibrated against
     * a real ride - no physical device has run this firmware/app pairing
     * in this sandbox. Typical road vibration is roughly a few
     * hundredths of a g between the ~2Hz BLE samples; sharp
     * accel/brake/pothole events push noticeably past that - but the
     * exact cutoffs should be tuned against real recorded rides once
     * available (e.g. look at avgAccelJerkG across a deliberately smooth
     * ride vs a deliberately aggressive one and set the boundaries
     * in between).
     */
    private fun ridingStyleFromAccel(rides: List<RideSession>): String {
        val avgJerk = rides.map { it.avgAccelJerkG }.average()
        return when {
            avgJerk > 0.12 -> "Aggressive - frequent sharp accelerations and braking"
            avgJerk > 0.05 -> "Balanced - mixed pace riding"
            else -> "Smooth - steady, consistent pace"
        }
    }

    /**
     * v1 fallback heuristic (pre-Phase-H): ratio of max-to-average speed
     * across recent rides as a rough "burstiness" signal - kept only for
     * rides that predate real accel data, see [ridingStyleFrom] above.
     */
    private fun ridingStyleFromSpeedBurstiness(rides: List<RideSession>): String {
        val burstRatio = rides
            .filter { it.avgSpeedKmh > 0 }
            .map { it.maxSpeedKmh / it.avgSpeedKmh }
            .average()

        return when {
            burstRatio > 1.6 -> "Aggressive - lots of bursts of speed"
            burstRatio > 1.25 -> "Balanced - mixed pace riding"
            else -> "Smooth - steady, consistent pace"
        }
    }
}
