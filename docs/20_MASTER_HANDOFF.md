# BikeOS - Master Handoff Prompt

Paste this whole document as the first message in a new chat, along with
the attached zip (upload it so Claude can read the real current code
before doing anything) - the builder names each session's output zip
`BikeOS_Phase_<X>` rather than `BikeOS_Handoff`, so the exact filename
will vary; any BikeOS zip attached alongside this doc is the one to read.

---

## What this project is

**BikeOS** (brand: VoidRoot, developer: Behnood Shafiei) is a smart
bicycle cockpit platform: an Android app (Kotlin + Jetpack Compose) paired
over BLE with a custom ESP32 hardware controller, turning a phone mounted
on a handlebar into a digital bike dashboard - live speed/cadence/battery,
lights, anti-theft alarm, calls/music widgets, and more.

- License: Proprietary (see `LICENSE`)
- Full history of every phase and every bug found+fixed:
  `docs/` folder, numbered files `00_...` through `24_...`, read in order
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
| **H (gear suggestions + riding analytics + keyless starter)** | ✅ Done (3/3) - not build-verified, see below |
| **I (final UI polish - Apple x Tesla redesign + 2 bug fixes)** | ✅ Done - not build/runtime-verified, HIGH PRIORITY to check on a real device (visual/subjective), see below |

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

### Phase H (3/3 done - full write-up in docs/22 and docs/23)

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

3. **Keyless starter - DONE.** Builder clarified: a generic Bluetooth
   camera-shutter remote (3 buttons - shutter + zoom in/out), repurposed
   as shutter=ignition-on, zoom buttons=arm/disarm. Since this is a
   Bluetooth HID accessory (not a custom BLE peripheral), it pairs to the
   **phone**, not the ESP32 - the phone intercepts its key events
   (`RemoteKeyHandler.kt`, wired into `MainActivity.onKeyDown()`) and
   forwards BLE Control Commands over the app's existing ESP32
   connection. New firmware command `BIKEOS_CMD_SYSTEM_ON` (0x50,
   additive, no protocol version bump) turns on all lights + fires a new
   non-blocking buzzer chime (`beepPattern()`) - required extracting the
   buzzer GPIO out of `alarm.cpp` into its own shared `buzzer/` module so
   the alarm's existing buzz and the new chime can't fight over the pin.
   Arm/disarm reuse the pre-existing `ARM_ALARM`/`DISARM_ALARM` commands
   unchanged. Full reasoning (esp. why the ESP32 doesn't talk to the
   remote directly - BLE HID host would've been unsafe to write untested
   here): `docs/23_PHASE_H_KEYLESS_STARTER.md`.

   **UNVERIFIED - top priority for the builder's next real-hardware
   session**: the exact `keyCode` each of the remote's 3 buttons sends
   was never confirmed (no physical remote in this sandbox).
   `RemoteKeyHandler.kt`'s `keyCodeMap` is a first guess - the doc above
   has step-by-step instructions (pair remote -> `adb logcat -s
   RemoteKeyHandler` -> press each button -> note the keyCode -> edit the
   map) to get the real mapping once the remote is in hand.

   Firmware version 0.6.0 -> 0.7.0, Android versionCode 15 -> 16
   (0.14.0 -> 0.15.0). **Phase H is now fully done** (all 3 parts).

### Phase I (DONE - full write-up in docs/24)

Builder's spec (their words, translated): modern/premium dark-mode UI,
an "Apple x Tesla" design fusion, the cluster (Dashboard) should read
like a modern car multimedia system crossed with BMW's cluster (theme
only - NOT an actual multimedia player to build), should feel like a
"million-dollar product" on open, use creative judgment, plus the
builder's own app icon artwork to wire in, plus 2 bugs to fix.

**Done:**
1. **App icon** - builder's provided artwork processed into a full
   mipmap set (legacy square/round + adaptive icon foreground/background
   at all 5 densities), old placeholder vectors deleted.
2. **Design system refresh** - `Color.kt`'s background deepened to true
   near-black (`#06070A`), primary accent shifted from neon cyan to a
   restrained "signal blue" (`#3E8EFF`, Apple/Tesla direction) with a
   refined indigo secondary (`#8B5CF6`) - same token NAMES kept, so
   every screen already reading them (all of them) picked up the new
   palette automatically, no per-screen edits needed. `Type.kt` tightened
   for a more "instrument readout" feel. `GlassCard` got a top-edge
   light gradient (real glass catches more light on top). `SpeedGauge`
   got instrument-cluster tick marks + a monospace digital readout - the
   concrete BMW-cluster-reference implementation.
3. **Splash screen** (new) - `androidx.core:core-splashscreen`, real
   launcher icon on the app's own background instead of a blank flash,
   custom fade+scale exit - the direct answer to "feels like a
   million-dollar product when it opens".
4. **Bug 1 (keyboard covers Signup's lower fields)** - root cause:
   `enableEdgeToEdge()` means `windowSoftInputMode` alone doesn't push
   content up anymore; fix is each form screen's own `.imePadding()`.
   Applied to Signup/Account/Settings/Calculator (every text-field
   screen), plus the manifest flag as a harmless fallback.
5. **Bug 2 (menu button drifts off-screen on some sizes)** - root cause:
   `BikeOSMenuScaffold` had zero status-bar inset handling, so on edge-
   to-edge it drew right under the status bar, worse on some devices'
   status bar heights than others. Fixed with real
   `Modifier.statusBarsPadding()` (reads actual WindowInsets, not a
   hardcoded offset) - same fix class applied to Onboarding's Skip
   button/bottom controls and Signup's submit button
   (`navigationBarsPadding()`), found via the same root-cause search.

**Still NOT done / explicitly out of scope for this pass:**
- This was a design-SYSTEM refresh (palette/typography/shared
  components/splash/one hero gauge), not a screen-by-screen visual
  rebuild - Home/Settings/About/Profile/Calculator's actual layouts are
  unchanged, they just inherit the new colors/type automatically. More
  BMW-multimedia-specific treatment of the Dashboard's other widgets
  (light toggles, mode selector, info cards) is a reasonable follow-up.
- Not build/runtime-verified - **this pass carries more risk shipping
  unverified than most others**, since it's almost entirely visual/
  subjective (contrast, icon rendering across real device mask shapes,
  splash timing) in a way static brace-balance checks can't catch. Get
  real device eyes on this before considering it truly finished.

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

All planned phases (F through J, the UI bug-fix pass, and I) are now
done per this doc - none are build/runtime-verified. Say something like:
*"Continue BikeOS. I tested it on a real device and found [bug/issue],
here's what happened: ..."* or *"Continue BikeOS, let's tune
[HALL_AO_THRESHOLD / the riding-style classifier thresholds / the remote
keyCode mapping] now that I have real hardware data."* Attach the zip so
Claude reads real code, not just this description.
