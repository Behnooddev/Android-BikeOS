# Phase H Summary - Gear Suggestions + Real Riding-Style Analytics

Scope confirmed with the builder mid-session: proceed with judgment on
exact scope. Two of the three original Phase H items are done below;
the third (keyless starter) has no spec yet - see "Not started" at the
bottom.

## 1. Gear suggestion algorithm

New file: `android/app/.../core/health/GearSuggestionEngine.kt`.

BikeOS has no gear-position sensor (gear is rider-synced, not sensed -
see `BikeRepository`'s kdoc), so this can't recommend an exact numeric
gear the way an electronic groupset with a derailleur position sensor
could. Instead it uses the same signal a rider's legs already give
them: cadence relative to an efficient pedaling band (70-90rpm), and
suggests a *direction* (easier/harder), gear-index-aware using the
currently-synced front/rear gear + the bike profile's gear counts - it
will never suggest shifting past the easiest/hardest gear the profile
says exists.

Convention (front index up = harder, rear index up = easier) matches
`CalorieCalculator`'s existing `gearEffortMultiplier` exactly, on
purpose - the two features must never disagree about what "harder"
means. If the builder's actual shifters number gears the opposite way,
both places need to flip together.

Wired into `DashboardViewModel`'s existing combine (added
`SettingsRepository` as a new dependency) and displayed as a small line
under the existing Gear widget in `DashboardScreen.kt`. Respects the
`gearSuggestionsEnabled` Settings toggle, which already existed in the
schema/Settings screen but was never actually connected to anything
until now.

## 2. Real MPU-based riding-style analysis

This replaced the old "ratio of max-to-average speed" heuristic in
`HomeViewModel.ridingStyleFrom()` with a real accelerometer-based
classifier - which required a genuine **BLE protocol change**, done
carefully per `CONTRIBUTING.md`'s golden rule (`bikeos_protocol.h` and
`BlePacket.kt` edited together, in the same pass):

- **Protocol 1.1 -> 1.2**: Sensor Data payload grew from 5 to 7 bytes,
  adding `accelMilliG` (u16 LE) - `motion::getAccelMagnitude() * 1000`,
  clamped to fit. Previously the MPU6050 was firmware-internal only (used
  by the anti-theft alarm) and never left the ESP32 at all.
- `firmware/src/bluetooth/ble_service.cpp` now includes `motion.h` and
  packs the new field; `bikeos_protocol.h`'s payload-size comment/define
  updated; `device_config.h`'s `BIKEOS_PROTOCOL_VERSION` bumped, firmware
  version bumped 0.5.0 -> 0.6.0.
- `BlePacket.kt`: `SensorPayload` gained `accelG: Float`, decode updated,
  `SENSOR_PAYLOAD_SIZE` bumped to 7.
- `SensorRepository.kt`: `SensorSnapshot` gained `accelG` (0f while
  disconnected - same "0 = no data" sentinel convention as every other
  field there; 0g isn't a physically real at-rest reading, so it's
  unambiguous).

**Live aggregation** (`DashboardViewModel`'s `RideAccumulator`): while a
ride is active, tracks "jerk" - frame-to-frame change in accel
magnitude between consecutive BLE samples (~2Hz, not a fixed-timestep
physics derivative, but the closest signal actually available from a
~2Hz notify rate) - and averages it into `avgAccelJerkG`, saved once at
ride end alongside the other aggregates (never per-tick, same as
everything else in `RideAccumulator`). Explicitly guards against
counting a BLE disconnect as a fake "jerk" spike.

**Storage**: `RideSessionEntity` gained `avgAccelJerkG: Float = 0f`,
`RideRepository`'s `RideSession` domain model mirrors it,
`BackupManager.kt`'s ride export includes it. Room schema bumped 2 -> 3
via `MIGRATION_2_3` (`ALTER TABLE ride_session ADD COLUMN avgAccelJerkG
REAL NOT NULL DEFAULT 0.0`), registered in `DatabaseModule.kt` alongside
the existing `MIGRATION_1_2`.

**Classifier** (`HomeViewModel.ridingStyleFrom`): prefers the real
accel-based classifier when at least 3 recent rides have real accel
data (`avgAccelJerkG > 0f` - the "0 = no data" convention doubles as
the fallback trigger here). Falls back to the old speed-burstiness
heuristic for rides recorded before this existed. Thresholds
(`0.12`/`0.05` g average jerk) are a first-pass estimate, explicitly
flagged in the kdoc as uncalibrated against any real ride - needs
tuning once the builder has real recorded rides to look at.

## Not build/runtime-verified
Same sandbox limitation as every other phase: no PlatformIO toolchain,
no Android/Gradle runtime, no real MPU6050/BLE link to record an actual
ride against. Static checks only (brace/paren balance across every
edited file). The Room migration in particular should be sanity-checked
on a real device with existing app data before trusting it blindly.

## Not started: keyless starter concept
Per `docs/20_MASTER_HANDOFF.md`, this was flagged from the start as
having no spec - hardware concept, no components confirmed. Needs the
builder's input on what hardware they have in mind (relay + the
existing button/BLE stack? A dedicated NFC/BLE-proximity unlock? Some
other mechanism?) before any code can be written for it.
