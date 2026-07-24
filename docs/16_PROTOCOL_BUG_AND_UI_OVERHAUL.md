# Protocol Bug Fix + UI/UX Overhaul

## Firmware compile bug (reported by builder, real bug)
`ble_service.cpp` referenced `BIKEOS_MSG_TYPE_ALARM_EVENT` (added in Phase
D) but it was never defined - `ble_uuids.h` had message-type constants
scattered in with GATT UUIDs, and the alarm one was simply missed.

**Fix - architectural, not just patching the one constant:**
- New `firmware/src/protocol/bikeos_protocol.h` - the single source of
  truth for EVERY message type, event ID, and command ID used anywhere in
  the firmware (message types, sensor/button/alarm payload sizes and
  values, and all 18 control command IDs - previously bare magic numbers
  in `ble_service.cpp`'s switch statement).
- `ble_uuids.h` now contains ONLY GATT service/characteristic UUIDs - a
  different, lower-layer concern, deliberately kept separate.
- `ble_service.cpp` rewritten to use named constants everywhere - zero
  magic numbers left in the packet-building or command-handling code.
- Audited: every `BIKEOS_MSG_TYPE_*`/`BIKEOS_CMD_*`/`BIKEOS_BUTTON_EVENT_*`/
  `BIKEOS_ALARM_EVENT_*` reference in the firmware now resolves to exactly
  one definition, and matches `android/.../data/ble/BlePacket.kt` exactly
  (verified byte-for-byte, both directions).
- `BlePacket.kt` now explicitly documents itself as the Android-side
  mirror of `bikeos_protocol.h`, and explains why this bug happened, so
  future protocol additions go in both places together instead of drifting.

## Build environment bugs (reported by builder, real bugs)
- `android/gradle.properties` never existed - added with
  `android.useAndroidX=true` / `android.enableJetifier=true` (required,
  without it nothing resolves) plus standard JVM/Kotlin defaults.
- Missing `import androidx.compose.runtime.getValue` in 4 files
  (`DashboardScreen.kt`, `LightControlRow.kt`, `RideModeSelector.kt`,
  `SpeedGauge.kt`) - the `by` property-delegate syntax on `State<T>`
  doesn't compile without it. Audited the ENTIRE codebase for this same
  missing-import pattern (not just the 4 reported files) - found and fixed
  all 4, confirmed no others.
- `quadraticTo()` isn't a real Compose Path API method - fixed to
  `quadraticBezierTo()` in `OnboardingGlyphIcon.kt`.
- `HorizontalPager`/`rememberPagerState` are experimental APIs - added
  `@OptIn(ExperimentalFoundationApi::class)` to `OnboardingScreen.kt`.

## UI/UX overhaul (per builder's design feedback)

**Orientation**: app is portrait by default now (manifest changed from
locked landscape). Only Dashboard and ClusterBoot force landscape,
programmatically (`LandscapeOnly.kt`), restoring the previous orientation
on exit - a phone held in the hand for Settings/Signup/etc. shouldn't be
sideways.

**No more redundant ride button**: removed the manual Start/Stop Ride
toggle from inside the cluster. Ride tracking is now fully automatic -
entering Dashboard starts it, Exit (button OR system back, both routed
through the same `DashboardViewModel.exitCluster()`) stops and saves it.
One less "start" concept for the rider to understand.

**Signup + Bike Configuration combined**: `SignupScreen` now collects bike
name/type/wheel size/gear counts in the same form as account creation,
saved together in one flow. Settings > Bike Configuration still exists for
editing it later.

**Menu**: `AppMenuButton` redesigned with icons per item (Home, Calculator,
Settings, About, Profile) and proper styling, AND is now present on every
menu-level screen (previously Home only) via a shared `MenuScreenHeader` -
switching between menu pages no longer requires backing out to Home first.
Uses the standard bottom-nav-style back stack pattern (`popUpTo` the graph
start + `saveState`/`restoreState`) so the back stack doesn't grow
unbounded as the menu gets used.

**Home**: Start button changed from a circle to a rounded-rectangle pill
with a layered glow (ambient + spot shadow, ~subtle breathing animation)
per the ask.

**Cluster visuals**: `SpeedGauge` gained a soft ambient radial glow behind
it; `RideModeSelector` chips now show a glyph + label with a gentle scale-
up + glow border on selection instead of a flat color swap; the cluster
background is now a subtle vertical gradient instead of a flat fill.

**Settings**: completely restructured from a flat list into grouped
`GlassCard` sections (Theme & Display, Units & Alerts, Reminders &
Security, Bluetooth, Bike Configuration, Backup, Danger Zone), each with
an icon + title, and toggle rows now show a one-line explanation under
each label instead of a bare switch.

**Account/Profile**: redesigned with an avatar (initials, gradient
background) + name/username header, and the form split into two grouped
cards (Personal Info / Physical Stats) instead of one long flat list.

**About**: now has real content (brand, developer, license, short
description) in grouped cards instead of a single placeholder line.

## Known remaining gaps (not addressed this pass, flagged not hidden)
- Calculator screen still has no real logic (its own dedicated phase).
- GlassCard's "glass" look is still gradient+shadow+border, not true
  backdrop blur (documented limitation since Phase A - unchanged).
- "تنظیم هاد" (HUD/cluster settings reachability) - interpreted as
  "Appearance should be easy to find", addressed by keeping it one tap
  from the newly-prominent Settings screen; if a different meaning was
  intended, please clarify and it'll be revisited.
