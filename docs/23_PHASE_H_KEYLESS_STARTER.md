# Phase H (part 3) - Keyless Starter

## What "keyless starter" actually does here
BikeOS is a pedal bike, not a motor vehicle - there's no engine to
"start". So the closest useful equivalent is an "ignition on" action:
press one button and the bike's electronics wake up - lights on, a
confirmation chime - instead of the rider having to open the app and
tap each light toggle individually. The builder also wanted the same
remote's other two buttons to arm/disarm the anti-theft alarm (which
already existed - no new concept there, just a new physical trigger for
it).

## The remote: a generic Bluetooth camera-shutter remote
The builder clarified they're using a cheap monopod/selfie Bluetooth
shutter remote (3 buttons: shutter + zoom in/out) as this trigger - not
a custom BLE fob built for this project.

**This changes the architecture significantly.** These remotes are
**Bluetooth HID devices** (they pair to a phone/tablet as a keyboard-like
accessory and send standard Android key codes, e.g. volume up/down or
camera keys, which stock camera apps interpret as shutter/zoom) - they
are NOT custom BLE GATT peripherals with editable firmware or a
documented service/characteristic UUID.

### Why the ESP32 does NOT talk to this remote directly
The tempting-sounding approach - have the ESP32 pair to the remote
itself - would require the ESP32 to act as a **BLE HID host** (scan,
bond, subscribe to the remote's HID Report characteristic, parse HID
report descriptors for keycodes). This is a real, fairly advanced
firmware undertaking even with a compatible BLE stack, and is exactly
the kind of thing that's unsafe to write untested in this sandbox (no
PlatformIO toolchain, no real remote to bond against, no way to inspect
its actual GATT/HID report structure). Getting HID host bonding wrong
on real hardware can be finicky to debug even with the physical parts in
hand.

### What was built instead
The remote pairs to the **phone**, exactly as it was designed to (same
as if it were still being used for photos). The BikeOS Android app is
already the BLE *client* connected to the ESP32 - so instead of adding a
second BLE role to the firmware, the phone just intercepts the remote's
button-press key events and forwards a Control Command over the
**existing** BLE connection it already has.

```
[BT shutter remote] --(Bluetooth HID pairing)--> [Phone / BikeOS app]
                                                          |
                                                   (existing BLE link)
                                                          v
                                                     [ESP32 firmware]
```

This reuses 100% of the existing BLE Control Command channel - zero new
BLE roles, zero new pairing flows, zero new firmware-side Bluetooth
complexity.

## What was built

### Firmware
- **New shared `buzzer` module** (`firmware/src/buzzer/`) - the piezo
  buzzer's GPIO ownership moved out of `alarm.cpp` into its own module,
  because now TWO things need to drive it: `alarm.cpp`'s existing
  triggered-alarm buzz, and the new ignition chime. `buzzer.cpp` exposes
  a non-blocking `beepPattern(count)` (services itself from `poll()`,
  never uses `delay()` - keeps BLE/sensor polling responsive) plus a
  `lockForExternalControl()` so `alarm.cpp`'s direct pin control and the
  chime's pattern-based control can never fight over the same GPIO.
  `alarm.cpp` was refactored to go through `buzzer::drive()` instead of
  touching the pin itself - **behavior unchanged**, just moved.
- **New command**: `BIKEOS_CMD_SYSTEM_ON` (0x50) in `bikeos_protocol.h` -
  turns on all 3 lights + fires a 3-beep chime (`ble_service.cpp`'s
  command switch). No protocol *version* bump - this is an additive
  command ID, not a payload/wire-format change to an existing message
  (see `bikeos_protocol.h`'s existing versioning convention).
- Arm/disarm reuse the **existing** `BIKEOS_CMD_ARM_ALARM`/
  `BIKEOS_CMD_DISARM_ALARM` - no firmware change needed there at all.

### Android
- **`ControlCommand.SYSTEM_ON`** added to `BlePacket.kt`'s command enum
  (id 0x50, matching the firmware).
- **New `RemoteKeyHandler`** (`presentation/common/`) - a small
  `@Singleton` that maps a hardware `keyCode` to a `ControlCommand` and
  sends it via `BleRepository`. Wired into `MainActivity.onKeyDown()`
  activity-wide (not scoped to just the Dashboard screen), since a real
  ignition button should work regardless of which screen happens to be
  open.

## UNVERIFIED - needs the builder's real remote
**This is the most important caveat in this whole doc.** The exact
`keyCode` each of the remote's 3 physical buttons actually sends was
**never confirmed** - there was no physical remote available in this
sandbox to test against. `RemoteKeyHandler.kt`'s `keyCodeMap` is a
first-guess based on common cheap BT shutter remotes (`KEYCODE_CAMERA`/
`KEYCODE_VOLUME_DOWN` for the shutter, `KEYCODE_VOLUME_UP` for one zoom
button, `KEYCODE_MEDIA_PLAY_PAUSE` for the other) - it may well be wrong
for this specific remote model.

**Built-in self-service debugging**: any keyCode NOT in the map gets
logged (`Log.d("RemoteKeyHandler", "unmapped keyCode=...")`) instead of
silently doing nothing. To get the real mapping:
1. Pair the remote to the phone via Android's Bluetooth settings (same
   as pairing any Bluetooth accessory).
2. Open BikeOS, open Logcat (`adb logcat -s RemoteKeyHandler`).
3. Press each of the 3 buttons once, note the `keyCode=N` for each.
4. Edit `keyCodeMap` in `RemoteKeyHandler.kt` to match.

If the same keyCode logs for more than one physical button (possible on
some cheap remotes where zoom buttons piggyback on the volume rocker),
the simple 1:1 table won't disambiguate them - that would need a
different approach (timing/press-count based) and should come back as
its own follow-up once that's actually observed, not guessed at now.

## Also not build/runtime-verified
Same sandbox limitation as every other phase - no PlatformIO toolchain,
no Android/Gradle runtime. Static brace/paren-balance checks only.
