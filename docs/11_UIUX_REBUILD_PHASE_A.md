# UI/UX Rebuild - Phase A Summary

Complete restructuring of the app's entry flow and visual system per the
builder's detailed UI walkthrough (see chat history around this date) -
this supersedes parts of the original Phase 1-2 UI/UX docs.

## Navigation flow (new)
`Splash -> Onboarding (first run, 5 slides + Skip) -> Signup (first run,
with password) -> Home (permanent landing screen) -> [Start] -> Dashboard/
Cluster -> [Exit] -> back to Home`

- `AppStateRepository` tracks onboarding/signup completion; Splash reads it
  once and routes accordingly.
- Erase Data (Settings) wipes everything and routes back through Splash ->
  Onboarding again.
- Hamburger menu (Home only, top-left): Home / Calculator / Settings /
  About / Profile. Appearance is reached from inside Settings, not the
  main menu.
- Dashboard has a real Exit button (top-right in landscape) - `popBackStack()`
  back to Home, not just relying on system back.

## No more fake data
`FakeSensorDataSource` is deleted. `SensorRepository` now emits real BLE
data when connected, or honest zeros + `isConnected=false` when not (with
a real, ticking clock - that's system time, not fake sensor data).

## Theme system (two independent layers)
1. **App-wide Material theme** (Settings > Theme > Dark/Light toggle) -
   affects Home, Settings, Signup, etc. See `AppThemeViewModel`.
2. **Cluster color customization** (Appearance screen) - separate Day/Night
   palettes (5 roles each: primary, accent, background, card background,
   text), auto-selected by time of day (6am-6pm = day), applied ONLY
   inside Dashboard via `LocalClusterPalette`. Color picking uses a curated
   swatch set, not a full HSV picker (see `ColorRole.kt` kdoc for why).

## Immersive cluster
Dashboard hides system bars entirely (`ImmersiveMode.kt`) - swipe-to-reveal
still works, so the rider is never locked out. Every other screen uses
edge-to-edge with a transparent status bar so it visually blends with the
screen background instead of having its own solid-color band.

## New Settings toggles
Dark/Light theme, 24h/12h clock, gear suggestions on/off, anti-theft alarm
on/off (sends ARM_ALARM/DISARM_ALARM to the ESP32), reminder notifications
on/off, and a new **engine-start animation** toggle (Phase B implements the
animation itself - this just reserves the on/off switch now).

## Signup / Account
Password is collected at signup (hashed+salted locally - see
`PasswordHasher.kt`, not sent anywhere, no server yet). It doubles as the
anti-theft alarm's quick-disarm code (`AlarmGuard.kt` - a dialog that can
appear over any screen the instant the ESP32 reports the alarm triggered,
not just from Dashboard).

## Backup format
Renamed to `.bop` (still a plain ZIP internally, just BikeOS-branded) and
now includes theme colors + app state (onboarding/signup completion) so
restoring on a new phone picks up right where the old one left off.

## Database
Schema bumped to version 2 with `MIGRATION_1_2` (new `user_profile` shape,
new `app_state` and `theme_colors` tables, new `settings` columns). Not
runtime-verified (no Android environment available while writing it) -
test this migration path specifically before relying on it.

## Known gaps after Phase A (tracked for later phases per the builder's plan)
- Engine-start animation itself (Phase B)
- Calls / Calories / Music cluster widgets (Phase C)
- Real anti-theft firmware (motion.cpp, alarm.cpp, buzzer - Phase D)
- Reminder notification worker (Phase E)
- Calculator logic (Phase F)
- Wiring/battery guide + GitHub project files (Phase G)
- Gear suggestion algorithm / advanced analytics / keyless starter (Phase H)
