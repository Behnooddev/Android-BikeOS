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
     * v1 heuristic only: ratio of max-to-average speed across recent rides
     * as a rough "burstiness" signal. A real riding-style classifier using
     * raw IMU data (now that the MPU is wired - see Phase 4) is Phase 5
     * territory (Smart Features / advanced analytics) - this is
     * deliberately simple and derived from real stored ride data, not a
     * placeholder number.
     */
    private fun ridingStyleFrom(rides: List<RideSession>): String {
        if (rides.size < 3) return "Not enough data yet - go for a ride!"
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
