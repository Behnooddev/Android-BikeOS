# Phase 4 Summary - Hardware Integration

All hardware the builder has confirmed purchased is now wired for real -
no more stubs for these modules.

## Firmware

**Speed/Cadence (Hall sensors, MH Sensor Series)**
- ISR-based pulse counting (`sensors.cpp`), debounced, rolling 1s window
  RPM calculation. Reports raw `wheelRpm`/`cadenceRpm` - deliberately NOT
  km/h (see the architecture decision below).
- Pins: wheel = GPIO4, cadence = GPIO5. Adjust `WHEEL_HALL_PIN`/
  `CADENCE_HALL_PIN` in `sensors.cpp` if wired differently.
- `MAGNETS_PER_WHEEL`/`MAGNETS_PER_CRANK` constants (both default 1) - bump
  these if multiple magnets get added later for better low-speed resolution.

**Rear distance (VL53L1X)** - unchanged from the Phase 3 addendum, now
actually feeding into the loop via `poll()`.

**Lights (front/rear/body, MOSFET-driven)**
- `controls.cpp` drives 3 GPIO outputs (13/14/27) through logic-level
  MOSFETs. Two independent control paths, both hit the same setters:
  1. Physical Light button (GPIO32) - cycles Off → Front+Rear →
     Front+Rear+Body → Off, entirely on-device, works even if the phone
     isn't connected.
  2. App's Control Service commands (now actually acted on, not just
     logged) - the Dashboard's new Bike Control Panel toggles send these.

**Buttons (4 total)**
- Light (GPIO32): handled locally, see above.
- Mode (GPIO33), Gear Up (GPIO25), Gear Down (GPIO26): these don't change
  any firmware-local state - the firmware doesn't own ride mode or gear
  config, Android/Room does. Instead, a press sends a **Button Event**
  packet to Android over BLE, and Android decides what happens.

**Battery (INA219)** - `power.cpp` reads voltage/current every 2s, converts
to a 0-100% estimate via a linear voltage curve (calibrated for single-cell
Li-ion, 3.3V-4.2V - adjust `BATTERY_MIN_V`/`BATTERY_MAX_V` for a different
pack). Reports 0 (not a fake number) if the sensor never initialized.

## Architecture decision: wheel size stays on Android

The firmware does NOT know wheel circumference. It reports raw `wheelRpm`;
Android converts that to speed/distance using the wheel size already
stored in Room (`BikeProfile.wheelSizeInches`). Same reasoning as calories
(Phase 3): rider/bike-profile data lives with the phone's data layer, not
synced into the firmware. This IS a breaking change to the Phase 3 Sensor
Data payload - see below.

## Protocol changes (breaking, both sides updated together)

- **Sensor Data payload**: was `speed(4)+distance(4)+cadence(2)+battery(1)`
  = 11 bytes: now `wheelRpm(2)+cadenceRpm(2)+battery(1)` = 5 bytes.
- **New message type**: `BUTTON_EVENT (0x04)`, 1-byte payload (button ID),
  sent over the same Sensor Data characteristic - Android's `BlePacket.decode()`
  now returns a sealed `DecodedNotification` (`SensorData` | `ButtonEvent`)
  instead of a single fixed shape.
- **Control Service**: light commands (`0x01`-`0x06`) now have a real
  effect; everything else is still parsed + logged only.

## Android

- `SensorRepository`: converts `wheelRpm` → km/h using
  `BikeRepository`'s wheel size, accumulates distance from real elapsed
  time between BLE notifications (not a fixed tick, unlike the fake source).
- `BleManager`/`BleRepository`: new `buttonEvents: SharedFlow<DeviceButtonEvent>`.
- `DashboardViewModel`: collects `buttonEvents` - MODE cycles through
  `RideMode.entries`, GEAR_UP/DOWN call `BikeRepository.syncCurrentGear`
  (already clamped to the configured gear counts, so a device event can't
  push it out of range).
- `DashboardViewModel` also gained `LightState` + `toggleFrontLight()`/
  `toggleRearLight()`/`toggleBodyLight()` - optimistic local state, NOT a
  confirmed physical readback (the firmware doesn't send one back - see
  `LightState`'s kdoc for why that's an accepted limitation, not an
  oversight).
- New `LightControlRow` on the Dashboard (the spec's "Bike Control Panel"),
  large touch targets, sends real `ControlCommand`s.

## Known simplifications / still open

- Calories still 0 while BLE-connected (unchanged from Phase 3 - needs a
  MET-based formula against `UserRepository`'s weight, not built yet).
- Battery percent curve is a simple voltage estimate, not proper
  coulomb counting - fine for v1, revisit if it's inaccurate in practice.
- Light state shown in the app can drift from physical reality if the
  physical button is used after the app last set it (documented, not
  silently wrong - both paths are real, "last write wins" is expected).
- BMI270/MPU6050 (motion/slope) still not implemented - not purchased yet.
- No de-bounce/backoff on the BLE Button Event path - rapid button mashing
  sends rapid events; Android just processes each one it receives.

## How to verify
- Firmware: flash, watch serial log for `[Sensors] Hall ISRs attached`,
  `[Sensors] VL53L1X ready` (or `FAILED` if not wired yet),
  `[Power] INA219 ready` (or `FAILED`), `[Controls] Lights + buttons ready`.
- Spin the wheel/crank magnets by hand near their sensors - confirm the
  Dashboard's speed/cadence respond once connected over BLE.
- Press the physical Light button - lights should cycle even with the
  phone disconnected. Toggle a light from the new Dashboard control row -
  confirm the corresponding MOSFET output responds.
- Press the Mode/Gear buttons while connected - confirm the Dashboard's
  ride mode / gear display updates.
