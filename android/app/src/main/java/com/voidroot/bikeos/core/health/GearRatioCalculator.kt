package com.voidroot.bikeos.core.health

import kotlin.math.PI

/** Same wheel-circumference math used for real BLE speed conversion (see SensorRepository), reused here for the standalone Calculator. */
object GearRatioCalculator {

    /** Distance covered per single full pedal (crank) revolution, in meters. */
    fun developmentMeters(frontTeeth: Int, rearTeeth: Int, wheelSizeInches: Float): Float {
        if (rearTeeth <= 0) return 0f
        val wheelCircumferenceMeters = (wheelSizeInches * 0.0254f) * PI.toFloat()
        return wheelCircumferenceMeters * (frontTeeth.toFloat() / rearTeeth.toFloat())
    }

    /** Traditional "gear inches" metric (wheelSize x gear ratio) - widely used in cycling, kept alongside the metric development figure. */
    fun gearInches(frontTeeth: Int, rearTeeth: Int, wheelSizeInches: Float): Float {
        if (rearTeeth <= 0) return 0f
        return wheelSizeInches * (frontTeeth.toFloat() / rearTeeth.toFloat())
    }

    /** Speed at a given cadence for this gear/wheel combo - km/h. */
    fun speedAtCadence(frontTeeth: Int, rearTeeth: Int, wheelSizeInches: Float, cadenceRpm: Float): Float {
        val developmentM = developmentMeters(frontTeeth, rearTeeth, wheelSizeInches)
        return developmentM * cadenceRpm * 60f / 1000f
    }
}
