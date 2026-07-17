# BikeOS Phase Plan (tracking)

- [x] Phase 0 - Foundation: project structure, nav, firmware skeleton, docs
- [x] Phase 1 - Dashboard MVP: fake sensor data, animated speedometer,
      glass cards, theme system, ESP32 basic BLE advertising
- [x] Phase 2 - Local Data System: Room, user/bike profile, ride history,
      settings storage, backup structure
- [x] Phase 3 - BLE Integration: device discovery, connection management,
      live sensor updates (packet-format decision resolved - see Phase 3 summary)
- [x] Phase 4 - Hardware Integration: speed/cadence sensors, lights,
      buttons, rear distance sensor (all real - see Phase 4 summary; IMU
      still pending purchase)
- [~] Phase 4 - Hardware Integration (in progress - see Phase 4 summary):
      real Hall wheel/cadence RPM + VL53L1X rear distance wired into BLE
      (protocol bumped 1.0 -> 1.1). Lights/buttons/INA219 battery still
      stubbed pending hardware purchase.
- [ ] Phase 5 - Smart Features: Bike Brain, ride modes, gear suggestions,
      advanced analytics

Each phase must leave both `android/` and `firmware/` in a runnable state
before the next phase starts.
