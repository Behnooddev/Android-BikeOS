package com.voidroot.bikeos.core.health

/**
 * Approximate calorie burn from speed + rider weight + gear "hardness" -
 * a MET (Metabolic Equivalent of Task) based estimate, the same family of
 * formula fitness trackers use for cycling. This is NOT a lab-grade
 * measurement (that needs heart rate or power-meter data, neither of
 * which BikeOS has) - it's a reasonable v1 built from data BikeOS actually
 * has (speed, weight, gear ratio), not a placeholder number.
 *
 * calories/min = MET * weightKg * 3.5 / 200   (standard MET formula)
 */
object CalorieCalculator {

    /** Cycling MET values by speed band - roughly following ACSM's compendium of physical activities. */
    private fun metForSpeed(speedKmh: Float): Float = when {
        speedKmh < 1f -> 0f        // essentially stationary - don't accumulate burn while parked
        speedKmh < 16f -> 4.0f     // leisurely
        speedKmh < 19f -> 6.0f     // light-moderate effort
        speedKmh < 22f -> 8.0f     // moderate-vigorous
        speedKmh < 25f -> 10.0f    // vigorous
        speedKmh < 30f -> 12.0f    // racing/very fast
        else -> 15.8f              // very fast racing
    }

    /**
     * Harder gears (bigger front / smaller rear -> higher ratio) take more
     * effort at the same speed; easier gears take less. This is a mild
     * adjustment (+/-15% max), not a dominant factor - speed still drives
     * most of the estimate.
     */
    private fun gearEffortMultiplier(frontGear: Int, rearGear: Int, frontGearCount: Int, rearGearCount: Int): Float {
        if (frontGearCount <= 1 || rearGearCount <= 1) return 1.0f
        val frontFraction = (frontGear - 1).toFloat() / (frontGearCount - 1).coerceAtLeast(1)
        val rearFraction = 1f - (rearGear - 1).toFloat() / (rearGearCount - 1).coerceAtLeast(1) // smaller rear = harder
        val hardnessFraction = ((frontFraction + rearFraction) / 2f).coerceIn(0f, 1f)
        return 0.85f + (hardnessFraction * 0.30f) // 0.85x (easiest) .. 1.15x (hardest)
    }

    /** Calories burned during one tick of [elapsedSeconds] at the given conditions. */
    fun caloriesForTick(
        speedKmh: Float,
        weightKg: Int,
        elapsedSeconds: Float,
        frontGear: Int,
        rearGear: Int,
        frontGearCount: Int,
        rearGearCount: Int
    ): Float {
        if (weightKg <= 0 || elapsedSeconds <= 0f) return 0f
        val met = metForSpeed(speedKmh) * gearEffortMultiplier(frontGear, rearGear, frontGearCount, rearGearCount)
        val caloriesPerMinute = met * weightKg * 3.5f / 200f
        return caloriesPerMinute * (elapsedSeconds / 60f)
    }
}
