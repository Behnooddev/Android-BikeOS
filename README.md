# BikeOS

**A smart bicycle cockpit platform.** BikeOS turns an Android phone,
mounted on a handlebar, into a full digital bicycle dashboard - paired
over Bluetooth Low Energy with a BikeOS ESP32 hardware controller for
live sensor data, lighting control, and an anti-theft alarm.

- **Developer:** Behnood Shafiei
- **Brand:** VoidRoot
- **License:** Proprietary (see [`LICENSE`](./LICENSE))
- **Platform:** Android 10+ (API 29+), ESP32

---

## What's in this repo

```
BikeOS/
├── android/     Kotlin + Jetpack Compose app (Clean Architecture / MVVM / Hilt / Room)
├── firmware/    ESP32 firmware (PlatformIO, Arduino framework)
└── docs/        Architecture, phase-by-phase build history, and the wiring guide
```

## Features

- **Automotive-style live cluster** - animated speedometer, cadence,
  distance, gear, and battery, with a boot "engine-start" sweep animation.
- **Real BLE telemetry** - wheel/cadence RPM from Hall sensors, rear
  object distance (VL53L1X), battery (INA219), converted to speed/distance
  on the phone using the rider's actual bike/wheel profile.
- **Handlebar controls** - physical buttons for lights, ride mode, and
  gear sync/answer-a-call, so the rider doesn't need to touch the phone.
- **Anti-theft alarm** - buzzer + blinking lights if the bike is disturbed
  while armed (wheel movement or a sharp motion-sensor delta), with an
  instant quick-disarm dialog on the phone.
- **Calls, Music, and Calories widgets**, gear-ratio/speed/calorie
  calculator, day/night customizable cluster colors, ride history, local
  backup/restore (`.bop` format), and ride reminder notifications.

See `docs/` for the full, dated build history (every phase, every bug
found and fixed, in order) if you want the detailed "why" behind any of
the above.

## Getting started

### Android app
1. **First-time only:** the repo includes `gradlew`/`gradlew.bat` and
   `gradle/wrapper/gradle-wrapper.properties`, but NOT the binary
   `gradle/wrapper/gradle-wrapper.jar` (it couldn't be generated in the
   environment these files were authored in). Before your first build,
   generate it once with a local Gradle install:
   ```
   cd android
   gradle wrapper --gradle-version 8.7
   ```
   Or simply open `android/` in Android Studio - it will detect the
   missing wrapper jar and offer to generate/sync it for you
   automatically. After this one-time step, `./gradlew` works normally
   and CI builds will too.
2. Open `android/` in Android Studio (Hedgehog or newer recommended - the
   project uses KSP and Hilt).
3. Let Gradle sync (first run needs internet to resolve dependencies).
4. Run on a **physical device** (BLE doesn't work on emulators).

### ESP32 firmware
1. Open `firmware/` in VS Code with the PlatformIO extension (or use the
   PlatformIO CLI).
2. Connect the ESP32 over USB.
3. Build + upload (`pio run --target upload`), then open the serial
   monitor at 115200 baud to confirm it boots and starts advertising.

### Hardware
See [`docs/18_WIRING_GUIDE.md`](./docs/18_WIRING_GUIDE.md) for the full
pin table, sensor wiring, MOSFET/buzzer wiring detail, and a recommended
incremental assembly order.

## Architecture at a glance

- **Android:** Clean Architecture (`presentation` / `data` layers),
  MVVM, Repository pattern, Hilt DI, Room for local persistence,
  Kotlin Coroutines/Flow throughout, Jetpack Compose UI.
- **Firmware:** modular by responsibility (`sensors/`, `bluetooth/`,
  `controls/`, `power/`, `motion/`, `alarm/`, `protocol/`), with a single
  consolidated protocol header (`firmware/src/protocol/bikeos_protocol.h`)
  as the source of truth for every BLE message type, event ID, and command
  ID - mirrored exactly in `android/.../data/ble/BlePacket.kt`.
- **BLE protocol:** custom structured packets (not plain text), versioned
  header, XOR checksum, three GATT services (Device Info / Sensor Data /
  Control).

## Contributing

This is a proprietary, closed project - see [`LICENSE`](./LICENSE). If
you've been given access to contribute, see
[`CONTRIBUTING.md`](./CONTRIBUTING.md) for the workflow.
