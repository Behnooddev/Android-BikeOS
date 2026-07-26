# Changelog

All notable changes to BikeOS, in order. Versions correspond to
`android/app/build.gradle.kts`'s `versionName`. See `docs/` for the full
detailed write-up behind any entry below.

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
