# Phase B Summary - Engine-Start Boot Animation

## Flow (new)
Home's Start button no longer jumps straight to Dashboard - it now goes
through `ClusterBootScreen`:

1. **Connecting phase** (`ConnectingUi.kt`): requests Bluetooth permission
   if not already granted, calls `BleRepository.connect()`, shows a
   pulsing-circle "Connecting to your bike..." animation.
2. **Timeout fallback**: after 8 seconds without a connection, a
   "Continue without connection" button appears - never traps the rider
   waiting forever for hardware that isn't there or isn't in range.
   Permission denial also surfaces this immediately (via the new reusable
   `PermissionRationaleDialog`, which explains why Bluetooth is needed and
   offers a real "Open Settings" button - not just a re-prompt).
3. **Animation phase** (`EngineStartAnimation.kt`): once connected (or the
   rider chose to continue anyway), the real `SpeedGauge` component sweeps
   0 -> max -> 0 (reusing its existing spring animation rather than a
   separate implementation), with a short buzzer-style tone
   (`ToneGenerator`, no audio asset needed) + haptic pulse timed to the
   peak - a car-dashboard-style self-test moment.
4. Lands on Dashboard with `ClusterBoot` popped off the back stack, so
   Dashboard's Exit button still correctly returns to Home.

## Settings toggle
"Engine-start animation" (added in Phase A) now actually does something:
off skips straight from Connecting to Dashboard with no sweep/sound.

## Also immersive
`ClusterBootScreen` uses the same `ImmersiveMode()` as Dashboard - the
whole boot sequence feels like one continuous fullscreen experience, not
"connecting screen with system bars" then "cluster without them".

## Known simplifications
- Day is fixed 6:00-18:00 for connecting/animation visuals (they use the
  default cluster palette, not the resolved day/night one - Dashboard
  itself still gets the real palette). Minor and not worth the complexity
  of threading ThemeColorsRepository into a 10-second transition screen.
- No retry/backoff loop during Connecting - a single `connect()` call plus
  the passive timeout. If the scan callback itself fails silently (radio
  off, etc.), the timeout fallback still saves the flow, just without a
  specific error message explaining why.

## How to verify
Tap Start from Home with the ESP32 powered and in range - should show
Connecting briefly, then the gauge sweep + a short beep + a buzz, then
land on Dashboard already showing live data. Power off the ESP32 and
repeat - after ~8s, "Continue without connection" should appear and lead
to a Dashboard showing all zeros (never fake data). Toggle the Settings
switch off and repeat - should skip straight from Connecting to Dashboard
with no sweep/sound.
