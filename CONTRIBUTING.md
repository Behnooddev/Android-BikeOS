# Contributing to BikeOS

BikeOS is a proprietary project (see [`LICENSE`](./LICENSE)) - this guide
is for people who've been given direct access to contribute, not a public
open-source contribution process.

## Project structure

```
android/     Kotlin + Jetpack Compose app
firmware/    ESP32 firmware (PlatformIO)
docs/        Architecture docs + dated build history
```

## Before you touch the BLE protocol

The single most important rule in this codebase: **the Android and
firmware sides of the BLE protocol must always be edited together.**

- Firmware source of truth: `firmware/src/protocol/bikeos_protocol.h`
- Android source of truth: `android/app/.../data/ble/BlePacket.kt`

If you add a new message type, event ID, or command ID, add it to BOTH
files in the same change, with the same numeric value. A real bug
happened once from exactly this drifting apart (a message type used on
the firmware side with no matching definition) - see
`docs/16_PROTOCOL_BUG_AND_UI_OVERHAUL.md` for the story. Don't repeat it.

GATT service/characteristic UUIDs are a separate concern and live in
`firmware/src/bluetooth/ble_uuids.h` / `android/.../data/ble/BleUuids.kt`
- same "edit both together" rule applies.

## Android conventions

- Clean Architecture: `presentation/` (Compose UI + ViewModels) and
  `data/` (repositories, Room, BLE, etc.) - screens never import Room
  annotations or touch DAOs directly, only repositories.
- Hilt for DI - `@HiltViewModel` on ViewModels, `@Inject constructor` on
  repositories, add a `@Module` in `di/` only when something needs a
  `@Provides` function (e.g. Room itself).
- One repository per data concern, exposing domain models (not Room
  entities) to the rest of the app.
- If you add a Room entity field or a new entity, add a `Migration` in
  `data/local/BikeOSDatabase.kt` - never rely on destructive fallback.
- Every `by remember { mutableStateOf(...) }` or
  `by someFlow.collectAsStateWithLifecycle()` needs
  `import androidx.compose.runtime.getValue` (and `setValue` for `var`s) -
  a real, repeated source of build breaks in this project's history.

## Firmware conventions

- One module per responsibility (`sensors/`, `controls/`, `power/`,
  `motion/`, `alarm/`, `bluetooth/`) - a module shouldn't reach into
  another's private state; use its public header functions.
- `init()` + `poll()` naming convention for every module, called from
  `main/main.cpp`'s `setup()`/`loop()`.
- Non-blocking only - no `delay()` calls in `poll()` paths; use
  `millis()`-based timing windows (see `sensors.cpp`'s RPM windowing or
  `alarm.cpp`'s motion windowing for the established pattern).

## Testing changes

Neither side has been build-verified in an actual Android Studio /
PlatformIO environment as of this writing (see `docs/` for the full
history of build errors found and fixed after the fact) - always do a
real build + run before considering a change done, not just a read-through.

## Documentation

If you make an architecturally significant change (new phase, protocol
change, a real bug found+fixed), add a dated entry to `docs/` following
the existing numbering pattern - the goal is that `docs/` reads as a
complete, honest history of the project, including the mistakes, not just
a polished current-state description.
