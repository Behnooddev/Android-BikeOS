# Changelog

All notable changes to BikeOS, in order. Versions correspond to
`android/app/build.gradle.kts`'s `versionName`. See `docs/` for the full
detailed write-up behind any entry below.

## [0.14.1] - Visual overhaul: Home + Dashboard restyled to match new cockpit design references
### Changed
- No new features, no logic changes - a pure visual pass matching two
  design references the builder supplied (a landscape cockpit layout and
  a portrait home-screen layout), reskinning existing screens rather than
  touching any ViewModel/state/BLE code.
- **Dashboard**: rebuilt as a real 3-column cockpit layout - stat cards
  (Distance/Calories/Cadence/Gear) now stack vertically on the left
  instead of a bottom row; the speed gauge got a thinner "precision arc"
  look; ride-mode chips gained real icons (Cloud/RadioButtonChecked/Bolt/
  Terrain/ArrowDownward) instead of Unicode glyphs; light controls moved
  from a horizontal pill row into a vertical column of icon cards
  (Front/Rear/Body) on the right, matching the reference's circular
  icon-button style; the top status row (Connected/Battery/Exit) gained
  matching icons; the music widget's transport controls became real
  Material icons instead of emoji.
- **Home**: added a decorative distance ring (aviation-instrument style)
  above the greeting text, restyled the Start button to "START RIDE"
  with a play icon, and added small icons to the Total Distance/Riding
  Style cards.
- `presentation/common/BikeOSMenuScaffold.kt` - top bar gained an
  optional trailing-icon slot (`actions` param, unused by any screen yet)
  and a gradient-tinted bold title, for visual consistency with the new
  reference designs' branded top bars.
### Note
- Not build-verified (same sandbox limitation as always - see
  `docs/20_MASTER_HANDOFF.md`). Static brace/paren-balance checked per
  edited file. `Icons.Filled.Route`/`WbIncandescent`/`Terrain`/
  `RadioButtonChecked` are standard `material-icons-extended` glyphs but
  haven't been confirmed against this exact BOM version by a real build -
  flag it if any fails to resolve and it'll get swapped immediately.

## [0.14.0] - Phase H (part 1+2): gear suggestions + real riding-style analytics
### Added
- `core/health/GearSuggestionEngine.kt` - cadence-band-based gear shift
  direction suggestions (easier/harder), gear-index-aware using the
  synced bike profile. Wired into `DashboardViewModel`/`DashboardScreen`,
  respects the (previously unused) `gearSuggestionsEnabled` Settings
  toggle.
- **BLE protocol 1.1 -> 1.2**: Sensor Data payload gained `accelMilliG`
  (5 -> 7 bytes) - the MPU6050's accel magnitude, previously
  firmware-internal only (anti-theft alarm), now reaches Android.
  `firmware/src/bluetooth/ble_service.cpp`, `bikeos_protocol.h`,
  `BlePacket.kt` updated together per CONTRIBUTING.md. Firmware version
  0.5.0 -> 0.6.0.
- Real MPU-based riding-style classifier in `HomeViewModel` - replaces
  the old speed-burstiness heuristic when enough real accel data exists
  (falls back to the old heuristic for older rides). New
  `avgAccelJerkG` aggregate computed live during a ride
  (`DashboardViewModel`), stored via Room schema v2 -> v3
  (`MIGRATION_2_3`), included in `BackupManager`'s ride export.
- `docs/22_PHASE_H_SUMMARY.md`.
### Note
- Not build/runtime-verified (no PlatformIO/Gradle/Android runtime in
  this sandbox) - classifier thresholds and the Room migration in
  particular need real-world sanity-checking. See
  `docs/22_PHASE_H_SUMMARY.md`.
- Keyless starter (3rd Phase H item) not started - no hardware spec yet.

## [0.13.0] - Phase J: alternate sensor backends
### Added
- `firmware/src/config/sensor_backend_config.h` - compile-time `#define`
  switches to pick between VL53L1X/HC-SR04 (rear distance), library/raw-
  averaging MPU6050 (motion), and digital-DO/analog-AO + active-high/low
  Hall (wheel/cadence). Defaults match the original confirmed hardware.
- HC-SR04 ultrasonic backend for rear distance (`pulseIn()` timing),
  behind the same `getRearDistanceMm()`/`isRearSensorReady()` interface
  as VL53L1X.
- Raw-register MPU6050 reading path with rolling-average smoothing
  (`motion.cpp`), bypassing the Adafruit library entirely - for
  suspected counterfeit/inaccurate MPU6050 units.
- Analog-AO polling path for Hall wheel/cadence sensors (threshold-
  compared, debounced the same as the digital ISR path), plus an
  independent active-high/active-low polarity switch usable with either
  Hall mode.
- `docs/21_PHASE_J_SUMMARY.md`, `docs/18_WIRING_GUIDE.md` "Alternate
  hardware" section.
### Note
- Not build-verified against real HC-SR04/clone-MPU6050/AO-Hall hardware
  (no PlatformIO toolchain in this sandbox) - see
  `docs/21_PHASE_J_SUMMARY.md` for what still needs bring-up tuning
  (notably `HALL_AO_THRESHOLD`).

## [0.12.0] - UI bug-fix pass: full-height drawer menu
### Changed
- Replaced the old small `DropdownMenu` (`AppMenu.kt` /
  `MenuScreenHeader.kt`) with a real full-height side drawer
  (`BikeOSDrawer.kt` + `BikeOSMenuScaffold.kt`), matching the previous
  app "Lumen"'s navigation pattern. All 6 menu screens (Home, Calculator,
  About, Settings, Account/Profile, Appearance) now use
  `BikeOSMenuScaffold`.
- Home screen's greeting text no longer overflows off-screen
  (`fillMaxWidth()`, smaller font, `maxLines=3` + ellipsis safety net).
- Dashboard cluster's middle section (gauge + ride mode chips + light
  toggles) restructured from a `Column` into a `Row` so it fits inside
  landscape's limited height without hiding the mode selector; `SpeedGauge`
  shrunk from 280dp/260dp to 220dp/200dp; `RideModeSelector` got a
  horizontal-scroll safety net + width constraint.
### Removed
- Dead files `presentation/common/AppMenu.kt` and
  `presentation/common/MenuScreenHeader.kt` (superseded by
  `BikeOSMenuScaffold`; confirmed no remaining imports before deletion).
### Note
- Not yet visually verified on a real device/emulator (no Android
  environment available in this sandbox) - drawer spacing, gradient
  header, and 280dp width may need adjustment once actually seen.

## [0.11.0] - Phase F
### Added
- Real Calculator screen: Speed/Distance/Time solver, Gear Ratio
  (development + gear-inches + speed-at-cadence), and Calorie estimate -
  each with a result and a short reaction comment based on magnitude.

## [0.10.2] - Build fix
### Fixed
- Release build (`lintVitalRelease`) failure: disabled the default
  `WorkManagerInitializer` via the official AndroidX Startup
  `tools:node="remove"` override, since `BikeOSApplication` supplies its
  own `Configuration.Provider` for Hilt-injected `ReminderWorker`.

## [0.10.1] - Build fix
### Fixed
- `ReminderWorker.kt`: invalid Kotlin call syntax for the `Flow.first()`
  extension function (`kotlinx.coroutines.flow.first(flow)` isn't valid
  Kotlin - needed `flow.first()` dot-call syntax with a proper import).

## [0.10.0] - Protocol bug fix + UI/UX overhaul
### Fixed
- Firmware compile error: `BIKEOS_MSG_TYPE_ALARM_EVENT` used but never
  defined. Root-caused to scattered protocol constants; fixed by creating
  `firmware/src/protocol/bikeos_protocol.h` as the single source of truth
  for every message type/event ID/command ID.
- Missing `gradle.properties` (AndroidX not enabled).
- Missing `import androidx.compose.runtime.getValue` in 4 files.
- `quadraticTo()` -> `quadraticBezierTo()` in `OnboardingGlyphIcon.kt`.
- Missing `@OptIn(ExperimentalFoundationApi::class)` for `HorizontalPager`.
### Changed
- App is portrait by default; only Dashboard/ClusterBoot force landscape.
- Removed the redundant manual Start/Stop Ride toggle - ride tracking is
  now automatic (starts on cluster entry, saves on Exit/back).
- Signup now collects Bike Configuration in the same flow as account setup.
- Hamburger menu redesigned with icons, now present on every menu screen
  (not just Home) with proper back-stack handling.
- Home's Start button changed from a circle to a glowing rounded-rect pill.
- Settings and Profile screens restructured into grouped, icon-labeled cards.
- Cluster visuals: ambient glow behind the speed gauge, redesigned ride
  mode chips with glyphs and a scale/glow selection animation.

## [0.9.0] - Phase E
### Added
- Ride reminder notifications (WorkManager): fires near the rider's
  learned average ride-start time if they haven't ridden yet today, goes
  quiet after 15 days of the app not being opened.

## [0.8.0] - Phase D
### Added
- Real anti-theft alarm: MPU6050 motion sensing + wheel-pulse detection,
  buzzer + blinking lights on trigger, BLE alarm events, app-side
  password-verified disarm dialog.

## [0.7.0] - Phase C
### Added
- Calls widget (contact name, answer/reject via handlebar buttons).
- Calories widget (real MET-based estimate, replacing a hardcoded zero).
- Music widget (generic now-playing control via MediaSessionManager).
### Fixed
- Missing launcher icon resources (referenced in the manifest since Phase
  0, never actually created) - added a real adaptive icon.

## [0.6.0] - Phase B
### Added
- Engine-start boot animation (speed gauge sweep + tone + haptic) between
  the Connecting screen and the Dashboard, toggleable in Settings.

## [0.5.0] - Phase A
### Added
- Full onboarding flow: Splash -> Onboarding (5 slides) -> Signup (with
  password) -> Home (permanent landing screen).
- Two-layer theme system: app-wide dark/light, plus independent
  day/night cluster color customization.
- Fully immersive Dashboard (system bars hidden).
- No more fake sensor data anywhere - real zeros + Disconnected state
  when not connected to the ESP32.

## [0.4.0] - Phase 4
### Added
- Real hardware integration: Hall-sensor wheel/cadence RPM, VL53L1X rear
  distance, INA219 battery, MOSFET-driven lights, 4 handlebar buttons.

## [0.3.0] - Phase 3
### Added
- Real BLE integration: GATT services, structured packet protocol,
  device discovery/connection, live sensor data over Bluetooth.

## [0.2.0] - Phase 2
### Added
- Room database: user/bike profile, ride history, settings, dashboard
  widget config. Local `.bop`-format backup/restore.

## [0.1.0] - Phase 0/1
### Added
- Initial Android + firmware project scaffolding, basic navigation,
  Dashboard MVP with simulated data, basic BLE advertising.
