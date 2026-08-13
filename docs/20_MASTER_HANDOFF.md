# BikeOS - Master Handoff Prompt

Paste this whole document as the first message in a new chat, along with
the attached `BikeOS_Handoff.zip` (upload it so Claude can read the real
current code before doing anything).

---

## What this project is

**BikeOS** (brand: VoidRoot, developer: Behnood Shafiei) is a smart
bicycle cockpit platform: an Android app (Kotlin + Jetpack Compose) paired
over BLE with a custom ESP32 hardware controller, turning a phone mounted
on a handlebar into a digital bike dashboard - live speed/cadence/battery,
lights, anti-theft alarm, calls/music widgets, and more.

- License: Proprietary (see `LICENSE`)
- Full history of every phase and every bug found+fixed:
  `docs/` folder, numbered files `00_...` through `19_...`, read in order
  for the complete "why" behind any decision.
- `README.md`, `CONTRIBUTING.md`, `HOW_TO_USE.md`, `CHANGELOG.md`,
  `SECURITY.md` already exist at repo root - read `CONTRIBUTING.md`
  especially, it has the critical BLE-protocol-sync rule.

## Architecture in one paragraph

Android: Clean Architecture, `presentation/` (Compose UI + ViewModels) +
`data/` (repositories/Room/BLE), Hilt DI, Room persistence, Coroutines/
Flow. Firmware: modular by responsibility (`sensors/`, `bluetooth/`,
`controls/`, `power/`, `motion/`, `alarm/`, `protocol/`), with
`firmware/src/protocol/bikeos_protocol.h` as the single source of truth
for every BLE message type/event ID/command ID - it MUST always match
`android/app/.../data/ble/BlePacket.kt` exactly. A real bug happened once
from these drifting apart - don't repeat it.

## CRITICAL - read this before doing anything

**This conversation's sandbox environment reset mid-session at least
once**, wiping the working directory. Work was restored from the last
delivered zip and some fixes were re-applied by hand afterward, but this
means: **do not trust anything is "definitely done" - verify the actual
file contents in the attached zip before assuming a described fix is
present.** The summary below is accurate as of the zip being attached,
but if anything looks inconsistent with this doc, trust the zip's actual
code.

## What's fully done and working (as of this handoff)

- **Phases 0-4** (original foundation): Android+firmware scaffolding,
  Dashboard MVP, Room database, real BLE protocol, real hardware
  integration (Hall wheel/cadence, VL53L1X, INA219, MOSFET lights, 4
  handlebar buttons).
- **Phase A**: Full navigation rebuild - Splash → Onboarding (5 slides) →
  Signup (account + password + bike config, all in one flow) → Home
  (permanent landing screen). Two-layer theme (app-wide dark/light +
  cluster day/night colors). No fake sensor data anywhere.
- **Phase B**: Engine-start boot animation (gauge sweep + tone + haptic),
  toggleable in Settings.
- **Phase C**: Calls widget (contact name, answer/reject via handlebar
  Gear Up/Down), Calories widget (real MET-based estimate), Music widget
  (MediaSessionManager, needs Notification access permission).
- **Phase D**: Real anti-theft alarm - MPU6050 motion + wheel-pulse
  detection, buzzer + blinking lights, BLE alarm events, phone-side
  password-verified disarm dialog (`AlarmGuard`, appears over any screen).
- **Phase E**: Ride reminder notifications (WorkManager, learns the
  rider's average ride-start time, goes quiet after 15 days unused).
- **Phase F**: Real Calculator (Speed/Distance/Time solver, Gear Ratio,
  Calories), each with a reaction comment based on result magnitude.
- **Phase G**: Wiring guide finalized (`docs/18_WIRING_GUIDE.md`,
  includes buzzer/MPU6050), full GitHub project files (LICENSE, README,
  CONTRIBUTING, SECURITY, CODE_OF_CONDUCT, CHANGELOG, .gitignore,
  `.github/` issue/PR templates + CODEOWNERS + a suggested CI workflow).
- **Multiple real bugs found (by the builder building it) and fixed**:
  missing `gradle.properties`, missing launcher icon resources, 4 missing
  `getValue` Compose imports, invalid `quadraticTo()` call, missing
  `@OptIn` for HorizontalPager, `BIKEOS_MSG_TYPE_ALARM_EVENT` used but
  undefined in firmware (fixed by creating the consolidated
  `bikeos_protocol.h`), invalid `Flow.first()` call syntax, release-build
  `WorkManagerInitializer` conflict (fixed via the official AndroidX
  Startup `tools:node="remove"` override), Erase Data not resetting
  custom cluster colors, `AppStateRepository.recordRideStart()` written
  but never actually called (learned ride-time was always the default).

## UI bug-fix pass - now DONE (as of this handoff)

The most recent work was a UI overhaul responding to this builder
feedback (their words, translated): *"the mode selector section is
invisible in the cluster, the greeting text on Home overflows off-screen,
and the menu is ugly - it should be a real full-height side drawer like
our previous app 'Lumen', not a small dropdown."*

**Done:**
1. Home screen greeting text overflow - fixed (`fillMaxWidth()`, smaller
   font, `maxLines=3` + ellipsis safety net). File:
   `presentation/menu/home/HomeScreen.kt`.
2. Dashboard cluster layout - the middle section (gauge + ride mode chips
   + light toggles) was a tall `Column` that overflowed landscape's
   limited height, hiding the mode selector. Restructured into a `Row`
   (gauge on the left, mode+lights stacked in a narrower column on the
   right) - fits landscape much better. File:
   `presentation/dashboard/DashboardScreen.kt`. Also shrank `SpeedGauge`
   from 280dp/260dp to 220dp/200dp to help it fit
   (`presentation/dashboard/components/SpeedGauge.kt`), and added a
   horizontal-scroll safety net + width constraint to
   `RideModeSelector.kt` so it can never clip regardless of available
   width.
3. New full-height drawer menu built: `presentation/common/BikeOSDrawer.kt`
   (`BikeOSDrawerContent` - the actual full-height `ModalDrawerSheet` with
   a branded gradient header, icon+label nav items with a selected-state
   highlight, footer) and `presentation/common/BikeOSMenuScaffold.kt`
   (`BikeOSMenuScaffold(navController, title) { content }` - wraps
   `ModalNavigationDrawer` around a screen, small top row with a menu
   icon that opens the drawer).
4. **All 6 screens now migrated to `BikeOSMenuScaffold`**: `AboutScreen.kt`
   and `CalculatorScreen.kt` (done earlier), plus `HomeScreen.kt`,
   `SettingsScreen.kt`, `AccountScreen.kt`, and `AppearanceScreen.kt`
   (done in this pass). Home's old hand-rolled top row (`AppMenuButton` +
   "BikeOS" title + balancing spacer) was removed and replaced by wrapping
   its content in `BikeOSMenuScaffold(navController, "BikeOS")` - kept the
   "BikeOS" title text since the prior row already showed one, for visual
   continuity.
5. Dead old-menu files deleted: `presentation/common/AppMenu.kt` and
   `presentation/common/MenuScreenHeader.kt`. Verified first with
   `grep -rl "MenuScreenHeader\|AppMenuButton"` that nothing imported them
   anymore (the only remaining hits were the files' own definitions plus
   a docstring mention in `BikeOSMenuScaffold.kt` explaining what it
   replaces).
6. Bumped `versionCode`/`versionName` (13 → 14, 0.11.0 → 0.12.0) and added
   a `CHANGELOG.md` entry for this pass.

**Still NOT done - carried over:**
7. **Test the actual look** - this was all written without a real
   Android environment to render it in (same limitation as the whole
   project so far - see below). The drawer's exact visual polish
   (spacing, the gradient header, whether 280dp is the right drawer
   width) has not been visually verified and may need adjustment once
   actually seen on a device. Brace-balance was checked statically
   (`grep -o "{" | wc -l` vs `"}"` per edited file) but this is not a
   substitute for a real compile.

## Phase list (full, for the new chat's reference)

| Phase | Status |
|---|---|
| 0-4 (foundation) | ✅ Done |
| A (nav/theme rebuild) | ✅ Done |
| B (engine-start animation) | ✅ Done |
| C (calls/calories/music widgets) | ✅ Done |
| D (real anti-theft alarm) | ✅ Done |
| E (ride reminders) | ✅ Done |
| F (real calculator) | ✅ Done |
| G (wiring guide + GitHub files) | ✅ Done |
| **UI bug-fix pass (mode selector, greeting, menu drawer)** | ✅ Done - not yet visually verified on a device |
| **J (alternate sensor backends)** | ✅ Done - not build-verified, see below |
| **H (gear suggestions + riding analytics)** | 🔶 2/3 done - not build-verified, see below |
| I (final UI polish, reserved for last) | ⏳ Not started |

### Phase J - alternative sensor code (DONE)

Builder confirmed (asked before writing, per the note below) they want
**all three** alternates available simultaneously, and left the
activation approach to the assistant's judgement - went with independent
compile-time `#define` switches (not runtime auto-detect - unvalidatable
without real hardware in this sandbox, and a wrong auto-detect guess
silently picking the wrong backend is worse than the builder explicitly
flipping a switch before flashing).

**Done:**
1. **HC-SR04 instead of VL53L1X** for rear distance - trigger/echo GPIO +
   `pulseIn()` timing, behind the same `getRearDistanceMm()`/
   `isRearSensorReady()` interface. Includes a 5V-ECHO-needs-level-
   shifting wiring warning (real hazard if skipped).
2. **MPU6050 raw-averaging fallback** - raw I2C register reads
   (`PWR_MGMT_1` wake + 6-byte burst from `ACCEL_XOUT_H`), bypasses the
   Adafruit library entirely, rolling average (default window 8) before
   computing magnitude.
3. **Hall sensor AO (analog) mode with configurable active-high/low
   polarity** - polled (not ISR-driven, since analog pins can't trigger
   interrupts) inside the existing `poll()` call every `loop()`
   iteration, same debounce window as the digital ISR path. Polarity
   switch applies to both digital and analog modes.

All three live behind independent switches in the new
`firmware/src/config/sensor_backend_config.h` - defaults still match the
original confirmed hardware, so an un-edited checkout is unaffected.
Full write-up: `docs/21_PHASE_J_SUMMARY.md`. Wiring:
`docs/18_WIRING_GUIDE.md` "Alternate hardware (Phase J)" section.
`firmware/src/config/device_config.h`'s `BIKEOS_FIRMWARE_VERSION` bumped
0.4.0 -> 0.5.0 (BLE protocol/wire format untouched, so
`BIKEOS_PROTOCOL_VERSION` did NOT need to change - no CONTRIBUTING.md
protocol-sync obligation here since no message type/event/command ID was
added or changed).

**Still NOT done - needs the builder's real hardware:**
- Not build-verified (no PlatformIO toolchain in this sandbox) - only
  static checks were possible (`#if`/`#endif` and brace balance).
- `HALL_AO_THRESHOLD` is a placeholder guess - needs tuning against the
  real AO module during bring-up.
- No runtime auto-detection was built (wasn't requested).

### Phase H (2/3 done)

Builder said "continue" (delegated exact scope to the assistant's
judgment) when asked which pieces to tackle. Full write-up:
`docs/22_PHASE_H_SUMMARY.md`.

**Done:**
1. **Gear suggestion algorithm** - `core/health/GearSuggestionEngine.kt`,
   cadence-band-based (70-90rpm target), gear-index-aware using the
   synced bike profile (no gear-position sensor exists, so this suggests
   a direction - easier/harder - not an exact gear). Wired into
   `DashboardViewModel`/`DashboardScreen`, respects the
   `gearSuggestionsEnabled` Settings toggle (existed in the schema
   already but was never connected to anything until now).
2. **Real MPU-based riding-style analysis** - required a genuine BLE
   protocol change (1.1 -> 1.2), done per CONTRIBUTING.md's golden rule:
   `bikeos_protocol.h` + `BlePacket.kt` + `ble_service.cpp` all updated
   together to add `accelMilliG` to the Sensor Data payload (5 -> 7
   bytes). `DashboardViewModel` now tracks a live "jerk" (frame-to-frame
   accel change) aggregate per ride, saved as `avgAccelJerkG` via a new
   Room migration (schema 2 -> 3). `HomeViewModel.ridingStyleFrom()` now
   prefers the real accel-based classifier when enough rides have real
   data, falling back to the old speed-burstiness heuristic for older
   rides. Firmware version 0.5.0 -> 0.6.0, Android versionCode 14 -> 15
   (0.12.0 -> 0.14.0 - note 0.13.0 was firmware-only, Phase J).

**Still NOT done:**
- Not build/runtime-verified (no PlatformIO/Gradle/Android runtime in
  this sandbox) - the Room migration especially should be sanity-checked
  against a real device with existing app data. The riding-style
  classifier's thresholds (0.12/0.05 g avg jerk) are an uncalibrated
  first guess - tune against real recorded rides once available.
3. **Keyless starter concept - NOT started, still no spec.** Ask the
   builder what hardware they have in mind (relay wired into the
   existing button/BLE stack? NFC/BLE-proximity unlock? something else?)
   before writing any code for this.

### Phase I (reserved for last)
Final UI polish pass - specific items not yet defined, the builder wants
this explicitly last, after H and J.

## Known environment limitation - important context for the new chat

**Neither the Android app nor the firmware has ever been build-verified
in a real Android Studio / PlatformIO environment by Claude** - this
sandbox has no Android SDK or PlatformIO toolchain. All code has been
written carefully and reviewed by static reading (and every bug the
builder has reported from real builds has been root-caused and fixed
properly, not just patched), but real build verification only happens
when the builder actually compiles it. **Expect more of this pattern**:
builder reports a real compiler/lint error → find the actual root cause →
fix it properly → explain why it happened. Don't assume silence means
something works.

Also: **`android/gradle/wrapper/gradle-wrapper.jar` (binary) is still
missing** - couldn't be generated in this sandbox (no access to
services.gradle.org). `gradlew`/`gradlew.bat`/`gradle-wrapper.properties`
(text files) are present and correct. Before the first build:
```
cd android && gradle wrapper --gradle-version 8.7
```
or just open in Android Studio, which auto-generates it.

## Hardware pin reference (current, confirmed hardware)

| Pin | Function |
|---|---|
| GPIO21/22 | I2C bus - VL53L1X, INA219, MPU6050 (shared) |
| GPIO4 | Wheel Hall sensor (digital, active LOW) |
| GPIO5 | Cadence Hall sensor (digital, active LOW) |
| GPIO13/14/27 | Front/Rear/Body light MOSFET gates |
| GPIO23 | Buzzer (anti-theft alarm) |
| GPIO32/33/25/26 | Light / Mode / Gear Up / Gear Down buttons |

Full detail: `docs/18_WIRING_GUIDE.md`.

## How to talk to Claude in the new chat

Say something like: *"Continue BikeOS. First finish migrating the 4
remaining screens to BikeOSMenuScaffold per the handoff doc, then [pick:
Phase J / Phase H / whatever bug you found]."* Attach the zip so Claude
reads real code, not just this description.
