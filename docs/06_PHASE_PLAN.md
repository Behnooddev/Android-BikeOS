# BikeOS Phase Plan (tracking)

## Foundation phases
- [x] Phase 0 - Foundation: project structure, nav, firmware skeleton, docs
- [x] Phase 1 - Dashboard MVP: fake sensor data, animated speedometer,
      glass cards, theme system, ESP32 basic BLE advertising
- [x] Phase 2 - Local Data System: Room, user/bike profile, ride history,
      settings storage, backup structure
- [x] Phase 3 - BLE Integration: device discovery, connection management,
      live sensor updates
- [x] Phase 4 - Hardware Integration: real Hall wheel/cadence RPM,
      VL53L1X rear distance, INA219 battery, MOSFET lights, 4 handlebar buttons

## UI/UX rebuild + feature phases
- [x] Phase A - Full navigation rebuild (Splash/Onboarding/Signup/Home),
      two-layer theme system, immersive cluster, no more fake data
- [x] Phase B - Engine-start boot animation (gauge sweep + tone + haptic)
- [x] Phase C - Calls/Calories/Music cluster widgets + launcher icon fix
- [x] Phase D - Real anti-theft alarm (MPU6050 + buzzer + BLE alarm events)
- [x] Phase E - Ride reminder notifications (WorkManager)
- [x] Phase F - Real Calculator (speed/distance/time, gear ratio, calories)
- [x] Phase G - Wiring guide finalized + GitHub project files
- [ ] Phase H - Smart Features: gear suggestion algorithm, advanced ride
      analytics, keyless starter (future hardware idea)
- [ ] Phase I - Final UI polish/updates pass (reserved for last, per the
      builder's request - specific items TBD when reached)

## Bug fixes / hardening done along the way (not separate phases, but real)
- Erase Data wasn't resetting custom cluster colors - fixed.
- 4 missing `getValue` imports, 1 invalid `quadraticTo` call, missing
  `@OptIn` for HorizontalPager, missing `gradle.properties` - all fixed.
- BLE protocol: `BIKEOS_MSG_TYPE_ALARM_EVENT` used but undefined - fixed
  by creating a single consolidated protocol header
  (`firmware/src/protocol/bikeos_protocol.h`).
- `ReminderWorker`: invalid Flow.first() call syntax - fixed.
- Release build: default WorkManagerInitializer conflicting with the
  app's own Configuration.Provider - fixed via the official AndroidX
  Startup override.

Each phase must leave both `android/` and `firmware/` in a runnable state
before the next phase starts.
