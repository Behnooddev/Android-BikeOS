# Phase J Summary - Alternate Sensor Backends

## Why
The builder has sensor units on hand that don't match the originally
assumed hardware/wiring:
1. HC-SR04 ultrasonic modules instead of the VL53L1X for rear distance.
2. Suspected counterfeit/inaccurate MPU6050 units - wants raw register
   reads instead of trusting the Adafruit library's calibrated output.
3. Hall modules that expose an analog "AO" pin (instead of/alongside
   digital "DO"), and/or are active-HIGH instead of the active-LOW
   assumption the original code made.

Builder confirmed (via the new chat) they want **all three** alternates
available, and left the activation mechanism to the assistant's
judgement. **Compile-time `#define` switches** were chosen over runtime
auto-detection - auto-detect logic can't be meaningfully validated
without real hardware in this sandbox (same build-verification
limitation as the rest of the project, see `docs/20_MASTER_HANDOFF.md`),
and a wrong auto-detect guess silently picking the wrong backend is a
worse failure mode than the builder explicitly flipping one `#define`
per swapped part before flashing.

## New file
`firmware/src/config/sensor_backend_config.h` - the single place to flip
backends. Three independent switch groups (mix and match freely):
- `BIKEOS_REAR_SENSOR_VL53L1X` / `BIKEOS_REAR_SENSOR_HC_SR04`
- `BIKEOS_MPU6050_LIBRARY` / `BIKEOS_MPU6050_RAW_AVERAGING`
- `BIKEOS_HALL_MODE_DIGITAL` / `BIKEOS_HALL_MODE_ANALOG`, plus an
  independent `BIKEOS_HALL_ACTIVE_LOW` / `BIKEOS_HALL_ACTIVE_HIGH`
  polarity switch that applies to either Hall mode.

Defaults are set to the original hardware (VL53L1X, library MPU6050,
digital active-low Hall) so an un-edited checkout behaves exactly as
before - the builder only touches this file for the parts they're
actually swapping.

## sensors.h / sensors.cpp
Public interface (`getRearDistanceMm()`, `isRearSensorReady()`,
`getWheelRpm()`, `getCadenceRpm()`, `getLastWheelPulseMs()`) is
unchanged - both distance backends and both Hall interfaces sit behind
the same functions via `#ifdef`/`#elif` blocks, so `ble_service.cpp` and
`alarm.cpp` need zero changes.

- **HC-SR04**: trigger/echo GPIO (configurable pins,
  `HCSR04_TRIG_PIN`/`HCSR04_ECHO_PIN`), `pulseIn()` timing with a bounded
  timeout so a disconnected sensor can't stall `poll()`, distance computed
  from round-trip time at the speed of sound. Explicitly documented that
  ECHO needs level-shifting from the sensor's 5V logic down to the
  ESP32's 3.3V GPIOs - a real wiring hazard if skipped.
- **Hall AO mode**: no interrupt is possible on an analog pin, so this is
  serviced by polling (`analogRead()` + threshold compare) inside the
  existing `poll()` call every `loop()` iteration instead of an ISR -
  same debounce window (`PULSE_DEBOUNCE_MS`) reused so behavior matches
  the digital path as closely as possible. Threshold
  (`HALL_AO_THRESHOLD`) is a starting guess that the builder will need to
  tune against a real module during bring-up (documented inline).
- **Active-HIGH polarity** flips the digital ISR's edge trigger
  (`FALLING` -> `RISING`) and the analog path's threshold comparison
  direction - one switch covers both Hall modes.

## motion.h / motion.cpp
Public interface (`getAccelMagnitude()`, `isReady()`) unchanged.

- **RAW_AVERAGING** path drops the Adafruit MPU6050 library dependency
  entirely for the sampling path (still shares the I2C bus `sensors.cpp`
  sets up) - talks to the chip with raw `Wire` register reads
  (`PWR_MGMT_1` wake write, then 6-byte burst reads from `ACCEL_XOUT_H`),
  deliberately touching as few registers as possible (no
  `ACCEL_CONFIG` write) to minimize what a clone chip could get wrong.
  Uses the chip's default power-on sensitivity (+/-2g, 16384 LSB/g).
  Maintains a small circular buffer (`MPU6050_RAW_AVERAGE_WINDOW`, default
  8 samples) per axis and averages before computing magnitude - smooths
  the kind of single-sample noise spikes cheap/fake accelerometer dies
  are prone to.

## Wiring guide
`docs/18_WIRING_GUIDE.md` updated with an "Alternate hardware (Phase J)"
section covering HC-SR04 wiring (including the 5V-to-3.3V ECHO
level-shifting warning) and the Hall AO wiring note - kept the original
confirmed-hardware pin table as the primary reference, alternates
clearly marked as such.

## Not done / needs the builder's real hardware
- **None of this has been build-verified or tested against real HC-SR04 /
  clone-MPU6050 / AO-Hall hardware** - same environment limitation as
  everything else (no PlatformIO toolchain in this sandbox). Preprocessor
  `#if`/`#endif` balance and brace balance were checked statically, but
  that's not a substitute for a real compile + hardware bring-up.
- `HALL_AO_THRESHOLD` is a placeholder - tune it against the real module
  (multimeter or a temporary `Serial.println(analogRead(...))` during
  bring-up) before trusting RPM readings from it.
- No runtime auto-detection was built (builder's call, see above) - if
  requirements change to "detect the wrong define and warn/fail loudly
  instead of silently misbehaving," that's a reasonable follow-up but
  wasn't asked for here.
