## Summary

What does this change do, and why?

## Component(s) affected

- [ ] Android app
- [ ] ESP32 firmware
- [ ] BLE protocol (both sides - see checklist below)
- [ ] Documentation only

## BLE protocol checklist (skip if not applicable)

If this PR adds/changes a message type, event ID, command ID, or payload
shape:

- [ ] Updated `firmware/src/protocol/bikeos_protocol.h`
- [ ] Updated `android/app/.../data/ble/BlePacket.kt` to match, exactly
- [ ] Verified both sides use the same numeric IDs and payload sizes

## Testing

How did you verify this works? (Real device build + run, not just a
read-through - see `CONTRIBUTING.md`.)

- [ ] Built and ran the Android app on a physical device
- [ ] Built and flashed the firmware to real hardware
- [ ] N/A (docs-only change)

## Checklist

- [ ] Follows the conventions in `CONTRIBUTING.md`
- [ ] Added/updated a dated entry in `docs/` if this is an architecturally
      significant change
- [ ] Updated `CHANGELOG.md`
