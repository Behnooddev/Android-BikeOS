# BikeOS

Smart bicycle cockpit platform — VoidRoot.
Turns an Android phone (handlebar-mounted) into a digital bicycle dashboard,
paired with an ESP32-based hardware controller over BLE.

- Developer: Behnood Shafiei
- Brand: VoidRoot
- License: Proprietary
- Min Android: 10 (API 29)

## Repo layout

```
BikeOS/
├── android/     Kotlin + Jetpack Compose app (Clean Architecture / MVVM)
├── firmware/    ESP32 firmware (PlatformIO)
└── docs/        Architecture & phase documentation
```

## Status: Phase 0 - Foundation

Both projects are structural skeletons only:
- Android: navigation graph wired, dashboard + 6 menu screens exist as
  static placeholders, dark glassmorphism theme tokens in place.
- Firmware: modular folder structure (sensors / bluetooth / controls /
  power / config), each module is a safe no-op stub, boots and logs
  device identity over serial.

No BLE, no Room database, no real sensor data yet - see `06_PHASE_PLAN.md`.
