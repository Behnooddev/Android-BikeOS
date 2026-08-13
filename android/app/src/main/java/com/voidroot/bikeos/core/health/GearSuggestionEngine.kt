package com.voidroot.bikeos.core.health

/**
 * v1 gear suggestion algorithm.
 *
 * BikeOS has no gear-position sensor - the current front/rear gear is
 * rider-synced, not sensed (see [com.voidroot.bikeos.data.repository.BikeRepository]'s
 * kdoc). So this can't tell you "shift to exactly gear 5" the way an
 * electronic groupset with a derailleur position sensor could. Instead it
 * uses the same signal a rider's own legs already give them - cadence
 * relative to an efficient pedaling band - and suggests a *direction*
 * (easier/harder), using the currently-synced front/rear gear + counts so
 * the suggestion is at least gear-index-aware (it won't ever suggest
 * shifting past the easiest/hardest gear the bike profile says exists).
 *
 * Convention (matches [CalorieCalculator]'s existing `gearEffortMultiplier` -
 * kept consistent so the two features never disagree about what "harder"
 * means): higher front gear index = bigger chainring = HARDER pedaling.
 * Higher rear gear index = bigger cog = EASIER pedaling. This is the
 * common derailleur/shifter numbering convention, but it IS an assumption
 * - if the builder's actual bike/shifters number rear gears the opposite
 * way, flip [easierGear]/[harderGear]'s direction below and in
 * `CalorieCalculator.gearEffortMultiplier` together (they must stay
 * consistent with each other).
 */
object GearSuggestionEngine {
    /** Efficient-pedaling cadence band (widely-cited road/MTB range) - below this, shift down; above, shift up. */
    private const val CADENCE_LOW_RPM = 70
    private const val CADENCE_HIGH_RPM = 90

    /** Below this speed, treat the rider as not really "riding" yet (starting off, stopped at a light) - no suggestion. */
    private const val MIN_MOVING_SPEED_KMH = 3f

    fun suggest(
        cadenceRpm: Int,
        speedKmh: Float,
        frontGear: Int,
        rearGear: Int,
        frontGearCount: Int,
        rearGearCount: Int
    ): GearSuggestion {
        if (speedKmh < MIN_MOVING_SPEED_KMH || cadenceRpm <= 0) return GearSuggestion.None

        return when {
            cadenceRpm < CADENCE_LOW_RPM -> easierGear(frontGear, rearGear, frontGearCount, rearGearCount)
            cadenceRpm > CADENCE_HIGH_RPM -> harderGear(frontGear, rearGear, frontGearCount, rearGearCount)
            else -> GearSuggestion.OnTarget
        }
    }

    /** Cadence too low (grinding) - recommend an easier gear. Rear derailleur preferred first: real riders shift rear far more often, and a rear-only step is smaller/smoother than a front shift. */
    private fun easierGear(front: Int, rear: Int, frontCount: Int, rearCount: Int): GearSuggestion = when {
        rear < rearCount -> GearSuggestion.ShiftDown(newFront = front, newRear = rear + 1)
        front > 1 -> GearSuggestion.ShiftDown(newFront = front - 1, newRear = rear)
        else -> GearSuggestion.AlreadyEasiest
    }

    /** Cadence too high (spinning out) - recommend a harder gear, same rear-first preference. */
    private fun harderGear(front: Int, rear: Int, frontCount: Int, rearCount: Int): GearSuggestion = when {
        rear > 1 -> GearSuggestion.ShiftUp(newFront = front, newRear = rear - 1)
        front < frontCount -> GearSuggestion.ShiftUp(newFront = front + 1, newRear = rear)
        else -> GearSuggestion.AlreadyHardest
    }
}

/** A gear suggestion - [label] is what the UI shows; the shift variants also carry the specific target gear indices in case a future UI wants to act on them directly (e.g. a one-tap "apply" button), not just display text. */
sealed class GearSuggestion(val label: String) {
    /** Not moving fast enough / not pedaling - nothing to suggest. */
    object None : GearSuggestion("")
    object OnTarget : GearSuggestion("Good cadence")
    object AlreadyEasiest : GearSuggestion("Already easiest gear")
    object AlreadyHardest : GearSuggestion("Already hardest gear")
    data class ShiftDown(val newFront: Int, val newRear: Int) : GearSuggestion("Shift down \u2193")
    data class ShiftUp(val newFront: Int, val newRear: Int) : GearSuggestion("Shift up \u2191")
}
