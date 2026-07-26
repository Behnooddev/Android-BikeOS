---
name: Bug report
about: Report something broken in the Android app or ESP32 firmware
title: "[Bug] "
labels: bug
assignees: ''
---

**Component**
- [ ] Android app
- [ ] ESP32 firmware
- [ ] BLE protocol (both sides)
- [ ] Documentation

**Describe the bug**
A clear description of what's wrong.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Tap on '...'
3. See error

**Expected behavior**
What you expected to happen instead.

**Logs**
If this is a build error, paste the full compiler/lint output. If it's a
runtime BLE issue, paste the relevant serial monitor output (firmware
side) and/or Logcat (Android side).

**Environment**
- Android app version (Settings > About, or `versionName` in
  `android/app/build.gradle.kts`):
- Firmware version (`BIKEOS_FIRMWARE_VERSION` in `device_config.h`):
- Android OS version / device model:
- ESP32 board model:

**Additional context**
Anything else relevant (e.g. hardware wiring differences from
`docs/18_WIRING_GUIDE.md`).
