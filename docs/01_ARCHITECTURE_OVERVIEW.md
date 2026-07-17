# BikeOS Architecture Overview

## Android
Clean Architecture, layered as:

```
presentation (Compose UI, ViewModels)
        ↓
domain (use cases)             — introduced Phase 2+
        ↓
data (repositories, Room, BLE) — introduced Phase 2/3
```

Phase 0 only populates `core/` (theme, navigation, shared components) and
`presentation/` (static screens). `domain/` and `data/` packages are
intentionally not created yet — they will appear with real logic in
Phase 2, to avoid empty scaffolding that goes stale.

## ESP32 Firmware
Modular folders, one responsibility each:

```
firmware/src/
├── config/     device identity constants
├── sensors/    speed, cadence, ToF, IMU, battery reads
├── bluetooth/  GATT services, packet protocol
├── controls/   buttons, light outputs
├── power/      battery monitoring, power states
└── main/       setup()/loop() wiring only - no logic lives here
```

## Open decisions (see Architecture Review)
1. Firmware framework: ESP-IDF vs Arduino+FreeRTOS - must be locked before
   Phase 3 (BLE) / Phase 4 (concurrent sensors).
2. BLE packet format: per-packet header will drop Device ID / Protocol
   Version (moved to one-time handshake) - to be finalized before Phase 3.
3. "Riding State" (Idle / Riding / Paused) as a single source of truth -
   to be introduced once Phase 1 produces real speed data.
