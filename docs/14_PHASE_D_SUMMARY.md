# Phase D Summary - Real Anti-Theft Alarm (Firmware)

Android side was already fully wired since earlier phases (AlarmGuard
dialog, BlePacket ALARM_EVENT decoding, ControlCommand ARM_ALARM/
DISARM_ALARM) - this phase implements the firmware side for real.

## New firmware modules

**`motion/` (MPU6050)** - Adafruit MPU6050 library, samples accel at 10Hz,
exposes `getAccelMagnitude()` in g. Shares the existing I2C bus (SDA=21,
SCL=22) with VL53L1X and INA219 - different addresses, no conflict.

**`alarm/`** - orchestrates the whole anti-theft flow:
- Two independent triggers (either fires it): a wheel Hall pulse detected
  while armed (bike physically rolled), or the MPU6050 accel magnitude's
  7-second-window average shifting by more than ~0.35g (bike picked up,
  dropped, shaken). The 7-second window mirrors the builder's original
  spec exactly.
- 3-second grace period after arming, so the act of arming itself
  (hand still on the bike) doesn't immediately trigger it.
- Once triggered: buzzer (GPIO23) + front/rear lights blink together,
  non-blocking (millis()-timed, doesn't stall the main loop).
- `arm()`/`disarm()` are called from BLE Control Commands only - the
  firmware never checks a password itself; Android verifies the account
  password locally, then sends `DISARM_ALARM` (see `AlarmGuardViewModel`
  from earlier phases).

## BLE additions
- New message type `ALARM_EVENT (0x05)`, 1-byte payload, sent over the
  same Sensor Data characteristic - edge-triggered (only on a triggered/
  cleared transition, not spammed every tick), matching the Button Event
  pattern.
- New Control Commands `ARM_ALARM (0x40)` / `DISARM_ALARM (0x41)`, wired
  to `alarm::arm()`/`alarm::disarm()` in `ble_service.cpp`'s command switch.

## Wiring
Buzzer: GPIO23 -> buzzer (through a transistor if it draws more than a
GPIO can source directly - see the wiring guide's MOSFET pattern; a small
passive piezo buzzer under ~20mA is usually fine straight off the pin).

## Known simplifications
- Motion threshold (0.35g) is a starting guess, not tuned against the real
  bike yet - expect to adjust `MOTION_DELTA_THRESHOLD_G` in `alarm.cpp`
  after real-world testing (too sensitive = false triggers from wind/
  traffic vibration; too loose = misses a real theft attempt).
- Disarming doesn't restore lights to their pre-alarm state - it just
  stops the blink loop and leaves lights wherever the last blink cycle put
  them. A future improvement could snapshot light state before triggering
  and restore it on disarm.
- No local disarm path on the device itself (e.g. a button combo) - only
  via the app's password dialog. Matches the spec (password lives in the
  app), but means a dead phone battery means no way to disarm from the
  bike side.

## How to verify
Arm the alarm from Settings, wait past the 3s grace period, then either
spin the wheel or pick up/shake the bike - buzzer + blinking lights should
start within a few seconds (immediately for the wheel-pulse path, up to
7s for the motion-delta path). Confirm the phone's AlarmGuard dialog pops
up automatically. Enter the wrong password - should show an error and stay
armed. Enter the correct password - alarm should stop.
