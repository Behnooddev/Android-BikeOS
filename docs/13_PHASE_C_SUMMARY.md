# Phase C Summary - Calls / Calories / Music Widgets + Build Fix

## Build fix (unrelated to Phase C, but blocking any build)
`AndroidManifest.xml` referenced `@mipmap/ic_launcher` / `ic_launcher_round`
with no actual resources behind them (Phase 0 created the folder but never
put anything in it) - AAPT failed resource linking. Added real adaptive
icon resources: `mipmap-anydpi-v26/ic_launcher.xml` (+ `_round`) pointing
at `drawable/ic_launcher_background.xml` (solid brand color) and
`drawable/ic_launcher_foreground.xml` (a simple vector gauge glyph
matching the SpeedGauge/onboarding visual language) - no PNG assets
needed, pure vector/XML so there's nothing to regenerate at different
densities.

Separately, the GitHub Actions log also showed:
`WARNING: ... sdk.dir property in local.properties file. Problem: Directory does not exist`
That's a CI environment issue, not a code issue - the workflow needs an
Android SDK set up (e.g. `android-actions/setup-android` or
`actions/setup-java` + manually installing `cmdline-tools`) before
`./gradlew` runs, and/or a `local.properties` generated pointing
`sdk.dir` at it. Not something fixable from inside the repo's source -
flag this back if the workflow YAML needs help too.

## Calories widget (real data now)
`CalorieCalculator` (MET-based estimate from speed + rider weight + gear
"hardness") replaces the hardcoded `calories = 0`. Accumulates
continuously in `DashboardViewModel` (same pattern as distance: Start/Stop
Ride just captures the delta) using real weight from `UserRepository` and
real gear from `BikeRepository`. Zero MET (no accumulation) below 1 km/h
so it doesn't creep up while parked.

## Calls widget
`CallRepository` watches call state via `TelephonyCallback`
(API31+)/`PhoneStateListener` (API29-30), resolves the contact name via
`ContactsContract.PhoneLookup`, and answers/rejects via `TelecomManager`.
Needs `READ_PHONE_STATE` + `READ_CONTACTS` + `ANSWER_PHONE_CALLS` (all
requested together from `DashboardScreen`, not assumed granted).

**Per the spec: while a call is ringing, the handlebar's Gear Up/Gear Down
buttons answer/reject it instead of adjusting the bike's gear** -
`DashboardViewModel.onDeviceButtonEvent` checks `incomingCall` first.
`CallWidget` is a banner that only appears while a call is actually
ringing (unlike the always-on bottom-row cards), showing the contact name
and a reminder of which button does what.

## Music widget
Generic now-playing control (works with whatever app - Spotify, YouTube
Music, etc. - has an active media session), via `MediaSessionManager` +
`MediaController`. This requires "Notification access", a special
permission only grantable from system settings (not a normal runtime
dialog) - `BikeOSNotificationListenerService` is a deliberately empty
listener service that exists purely to make BikeOS an authorized caller of
`getActiveSessions()`. `DashboardScreen` shows a tappable card linking
straight to the system settings page when access isn't granted yet,
instead of a broken/blank widget.

## New reusable widget keys
`WidgetKeys.CALLS` and `WidgetKeys.MUSIC` added to the Appearance toggle
list. Fixed a real bug in `DashboardConfigRepository.ensureSeeded()` while
doing this: it previously only seeded defaults on a fully-empty table, so
adding new widget keys later would never backfill them for
already-onboarded installs. Now it backfills any missing default keys
without touching existing ones' position/enabled state.

## Known simplifications
- Calls: pre-API26 devices have no non-system way to answer a call
  programmatically - moot here since minSdk is 29, but noted in code.
- Music: picks the FIRST active media session if multiple exist (e.g. two
  apps both holding a session) - no UI to choose between them.
- No retry loop if Notification access is revoked mid-ride - the widget
  will just stop updating; re-opening Dashboard re-checks.

## How to verify
- Grant the phone/contacts permissions when prompted, call the test phone
  from another device - contact name (or number) should appear in the
  Calls banner; test Gear Up (answers) and Gear Down (rejects) from the
  handlebar buttons.
- Enable Notification access via the in-app prompt, start playing music in
  any app - cover/title/artist should appear, and the play/pause/skip
  controls should actually control that app's playback.
- Go for a (simulated, via BLE test data or real riding) ride with a
  height/weight/age filled in - Calories widget should climb from 0 based
  on speed, and stop climbing when speed drops to ~0.
