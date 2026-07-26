# Phase F Summary - Real Calculator

## Three modes, per the product spec ("pick what to calculate, give it
inputs, get a result AND a reaction based on the output")

**Speed / Distance / Time** - pick which one to solve for, fill in the
other two. Standard kinematics (distance = speed x time, etc), with only
the two relevant input fields shown at a time.

**Gear Ratio** - front/rear teeth + wheel size gives development (meters
per pedal revolution) and traditional gear-inches; optionally add a
cadence to also get speed-at-that-cadence. Same wheel-circumference math
already used for real BLE speed conversion (`GearRatioCalculator.kt`
mirrors the logic in `SensorRepository`).

**Calories** - speed + weight (prefilled from the rider's profile, still
editable) + duration gives an estimate, via a new
`CalorieCalculator.estimateCalories()` (a gear-free sibling of the
real-time `caloriesForTick()` used on the Dashboard - the calculator
doesn't need live gear state for a hypothetical "what if" calculation).

## Reaction comments
Every mode's result comes with a short reaction based on magnitude (e.g.
"That's race-level fast!", "A hard gear - great for flat sprints, tough on
climbs", "Big calorie burn - that's a serious workout!") - thresholds are
reasonable judgment calls, not scientifically derived, and easy to retune
in `CalculatorViewModel`'s reaction functions if they feel off in practice.

## How to verify
Try each mode: Speed/Distance/Time solving for each of the three in turn
(confirm the right two fields show), Gear Ratio with and without a
cadence value, Calories with the weight field both auto-filled (has a
profile) and manually overridden.
