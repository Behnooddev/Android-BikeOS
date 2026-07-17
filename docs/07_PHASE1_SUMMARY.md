# Phase 1 Summary - Dashboard MVP

## Android
- `DashboardViewModel` + `FakeSensorDataSource`: simulated speed (clamped
  random walk, not pure noise), distance/calorie accumulation, cadence,
  battery drain, and a simulated "connecting for ~3s then connected" BLE
  state - all shaped like a real Sensor Data Service so Phase 3 can swap
  the data source without touching the ViewModel's combine() logic.
- `SpeedGauge`: 270° animated arc speedometer with a spring-animated fill
  and digital number, plus a layered soft-glow effect. Both fill and number
  animate smoothly (no instant jumps) per the UI/UX spec.
- `RideModeSelector`: Eco/Cruise/Sprint/Climb/Downhill chips, animated
  selection highlight. Mode-change recommendations are not implemented yet
  (that needs the alert system, later phase).
- Dashboard now shows: connection status, battery %, clock, speed gauge,
  ride mode, distance, calories, cadence, and gear (front x rear, static).

## ESP32 Firmware
- `bluetooth/ble_service.cpp` now does real BLE advertising (BLEDevice,
  Arduino ESP32 BLE library bundled with the core - no extra platformio
  dependency needed): sets device name, starts one placeholder service UUID,
  and advertises. No GATT characteristics/data exchange yet - that's Phase 3.

## Not in Phase 1 (by design)
- Widget add/remove/rearrange engine
- Persisted settings, gear config, user/bike profile (Room - Phase 2)
- Real BLE data exchange with the phone (Phase 3)
- Alerts (speed, rear-object, gear recommendation)
- Front/rear/body light control (Phase 4, per hardware roadmap)

## How to verify
- Android: open `android/` in Android Studio, run on a device/emulator
  (landscape) - dashboard should show the gauge animating within ~3-5s of
  launch and settle into a "Connected" state after ~3s.
- Firmware: open `firmware/` in PlatformIO, flash to an ESP32 dev board,
  open serial monitor at 115200 baud - should print device name + firmware
  version, then "[BLE] Advertising started...". Confirm with a BLE scanner
  app (e.g. nRF Connect) that "BikeOS Device" appears and is advertising the
  placeholder service UUID.
