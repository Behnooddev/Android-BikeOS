# Phase 2 Summary - Local Data System

## What was added

**Room database** (`data/local/`) - schema v1, 5 entities:
`user_profile`, `bike_profile`, `ride_session`, `settings`, `dashboard_widget`.
`exportSchema = true` so future migrations have a real baseline to diff
against. No destructive-migration fallback anywhere, per the Update/Security
spec's "never delete user data during updates."

**Repository layer** (`data/repository/`) - one repository per entity,
each exposing a small domain model (e.g. `UserProfile`, not
`UserProfileEntity`) so screens never touch Room types directly.

**Dependency injection** - Hilt is now wired (`@HiltAndroidApp`,
`@AndroidEntryPoint`, `@HiltViewModel`, `hiltViewModel()` in Compose).
`DatabaseModule` provides the `BikeOSDatabase` singleton and its DAOs.
This replaces Phase 0/1's plain `viewModel()` default-constructor pattern.

**Backup** (`data/backup/BackupManager.kt`) - real export/import to a ZIP
(`user.json` / `bike.json` / `settings.json` / `dashboard.json` /
`rides.json` / `metadata.json`), schema-version-checked before import.
Currently reads/writes app-private storage only; a file picker (Storage
Access Framework) for a real "choose where to save" UX is a follow-up UI
task, not implemented this phase. **Not yet encrypted** - flagged as a gap
in the architecture review, should be closed before shipping to real users.

## What's now functional (not just placeholder screens)

- **Account**: rider profile form (name/username/email/age/height/weight),
  saved to Room.
- **Settings**: unit system toggle, sound toggle, max-speed-alert value,
  full bike configuration (name/type/wheel size/gear counts/current gear
  sync), backup export/import buttons.
- **Appearance**: dashboard widget enable/disable toggles (Distance,
  Calories, Cadence, Gear - Speed stays always-on).
- **Dashboard**: gear now reads from the saved bike profile instead of a
  hardcoded value; bottom-row cards respect the Appearance toggles; a new
  Start/Stop Ride control persists a completed ride (duration, distance,
  calories, avg/max speed, avg/max cadence, ride mode) to `ride_session`.
- **Home**: ride history list reading from `ride_session`.

## Known simplifications (documented, not hidden)

- Settings' bike-config fields write to Room on every keystroke rather than
  batching into a draft + Save button - fine for Phase 2, worth debouncing
  later.
- Backup import intentionally does NOT restore ride history automatically
  (needs de-duplication logic against existing rows first).
- Unit system toggle is stored but the Dashboard doesn't yet convert
  displayed values to miles - it's read/write in Settings, just not
  consumed by the Dashboard's rendering yet.

## Not in Phase 2 (by design, later phases)

- Real BLE data exchange (Phase 3)
- Multi-bike / multi-device support
- Backup encryption, file-picker-based export/import location
- Drag-to-reorder widget UI (data model already supports `position`)

## How to verify

Open `android/` in Android Studio, run on a device/emulator. Fill in
Account, adjust Settings (bike config + units + sound), toggle a widget
off in Appearance and confirm its card disappears from the Dashboard, then
Start Ride → wait a few seconds → Stop Ride and confirm it appears in Home.
Kill and reopen the app - all of the above should persist.
