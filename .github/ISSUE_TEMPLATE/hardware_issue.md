---
name: Hardware / wiring issue
about: Report a problem with sensor wiring, pin conflicts, or physical assembly
title: "[Hardware] "
labels: hardware
assignees: ''
---

**Which module is affected?**
- [ ] Hall sensors (wheel/cadence)
- [ ] VL53L1X (rear distance)
- [ ] INA219 (battery)
- [ ] MPU6050 (motion / alarm)
- [ ] Lights (MOSFETs)
- [ ] Buttons
- [ ] Buzzer
- [ ] Power/battery

**Describe the issue**
What's not working as expected (e.g. sensor never reports "ready", erratic
readings, GPIO conflict).

**Your wiring**
Does it match `docs/18_WIRING_GUIDE.md` exactly? If not, describe the
difference (different GPIO pins, different sensor breakout, etc).

**Serial monitor output**
Paste the relevant `[Sensors]` / `[Power]` / `[Motion]` / `[Controls]`
log lines from boot.

**Board/part models**
- ESP32 board:
- Sensor breakout(s) and where purchased, if known:
