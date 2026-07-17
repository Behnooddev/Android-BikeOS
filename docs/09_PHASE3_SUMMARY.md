# Phase 3 Summary - BLE Integration

## Hardware note (builder-confirmed substitutions)
- Hall sensors: MH Sensor Series digital hall module instead of bare A3144
  - no firmware logic impact, same digital pulse output.
- Rear detection: HC-SR04 ultrasonic instead of VL53L1X for now
  - DOES change Phase 4's sensor code shape (trigger/echo GPIO + pulseIn(),
    not I2C). Documented in `firmware/src/sensors/sensors.h` so Phase 4
    starts from the right assumption. The rest of the firmware/BLE payload
    doesn't care which sensor produced the distance reading.

## Packet protocol (finalized, resolves architecture-review Conflict #2)
Header no longer repeats Device ID / Protocol Version per packet - those
are read once from the Device Information Service right after connecting.

```
[messageType:1][timestampEpochSec:4 LE][payloadLength:1][payload:N][checksum:1 XOR]
```

Sensor Data payload (11 bytes): speedKmh (float32) + distanceKm (float32) +
cadenceRpm (uint16) + batteryPercent (uint8). Calories are deliberately NOT
in this payload - they depend on rider weight, which only Android knows.

Defined in two places that must be kept manually in sync (no shared
code-gen yet - flagged as tech debt in the architecture review):
- `android/.../data/ble/BlePacket.kt` + `BleUuids.kt`
- `firmware/src/bluetooth/ble_uuids.h` + the packing code in `ble_service.cpp`

## GATT services (firmware side, real - not stubs anymore)
- **Device Information Service** (read): Device ID, Firmware Version, Protocol Version
- **Sensor Data Service** (notify): the Sensor Data packet above, sent every
  500ms while connected, built from a simulated generator (mirrors the
  Android FakeSensorDataSource) - real hardware reads are Phase 4.
- **Control Service** (write): validates checksum + message type + logs the
  command ID. Does not drive any GPIO yet - Phase 4 wires lights/gear
  effects once the physical outputs exist.

## Android side
- `BleManager`: scan (filtered on the Device Info service UUID) → connect →
  discover services → read device info once → subscribe to Sensor Data
  notifications → parse into `SensorPayload`.
- `BleRepository` / `SensorRepository`: `SensorRepository.stream()` is what
  the Dashboard actually consumes - it transparently switches between real
  BLE data (while `BleConnectionState.Connected`) and the Phase 1 fake
  generator otherwise, so the cockpit never goes blank on disconnect (per
  the Communication Rules spec).
- Runtime permissions (`BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` on API 31+,
  `ACCESS_FINE_LOCATION` below that) are requested from Settings >
  Bluetooth Configuration, not assumed pre-granted.
- Settings screen: connection status, connected device ID/firmware version,
  Scan & Connect / Disconnect buttons.

## Known simplifications
- Calories stay at 0 while BLE-connected (real per-rider calculation isn't
  implemented yet - noted inline in `SensorRepository`).
- Checksum is a simple XOR, not a cryptographic check - adequate for
  catching corrupt/truncated notifications, not for authentication (BLE
  security/bonding is still a later hardening pass per the architecture
  review).
- Control Service commands are validated and logged by the firmware but
  don't drive anything physical yet - nothing to drive until Phase 4.
- No reconnect/backoff strategy yet (flagged as a missing requirement in
  the architecture review) - a dropped connection currently requires
  tapping Scan & Connect again manually.

## How to verify
- Firmware: flash, confirm serial log shows advertising + (once a phone
  connects) "[BLE] Client connected" and periodic notify activity.
- Android: grant Bluetooth permission via Settings > Bluetooth
  Configuration > Scan & Connect, confirm status goes
  Scanning → Connecting → Connected, device ID/firmware version appear,
  and the Dashboard's speed/distance/cadence/battery start reflecting the
  ESP32's simulated values instead of the phone's own fake generator.
  Turn off the ESP32 - Dashboard should fall back to the fake stream
  instead of freezing or going blank.

## Addendum (post-Phase 3, ahead of formal Phase 4)

Per the builder's confirmed parts list:
- **VL53L1X rear-distance sensor code is implemented for real** in
  `firmware/src/sensors/sensors.cpp` (Pololu VL53L1X Arduino library,
  non-blocking `dataReady()`-gated reads, Long distance mode). This runs
  ahead of the formal Phase 4 kickoff since the part is confirmed - Hall
  effect speed/cadence ISR wiring and IMU/battery reads are still deferred
  to Phase 4 proper.
- **Not yet done**: wiring `getRearDistanceMm()` into the BLE Sensor Data
  payload/notifications, or into any alert logic - that's Alert System
  territory (later phase) and would also require extending the
  already-finalized Phase 3 packet payload on both sides. Flagging this
  explicitly rather than quietly expanding the wire protocol.
- **Lighting/Control hardware (MOSFET outputs) intentionally NOT
  implemented** - builder hasn't purchased that hardware yet. `controls.cpp`
  remains a no-op stub until parts are confirmed.
