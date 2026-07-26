# Phase G Summary - Wiring Guide + GitHub Project Files

## Wiring guide finalized
`docs/18_WIRING_GUIDE.md` fully updated for every Phase D addition
(buzzer on GPIO23, MPU6050 sharing the I2C bus) - complete pin table, I2C
bus notes for all three shared-bus sensors, MOSFET/buzzer wiring detail,
magnet placement notes, power/battery section, and a recommended
incremental assembly + test order (ESP32 alone -> I2C bus one sensor at a
time -> Hall sensors -> lights/buttons -> buzzer last).

## GitHub project files added (repo root)
- `LICENSE` - proprietary/all-rights-reserved, with an explicit hardware
  safety notice (lights/buzzer/sensor readings, hobbyist-use disclaimer)
  and a third-party-dependencies carve-out.
- `README.md` - project overview, repo structure, feature list, getting
  started for both Android and firmware, architecture summary.
- `HOW_TO_USE.md` - end-user guide (not a dev doc) covering first-time
  setup through daily riding, anti-theft, reminders, and troubleshooting.
- `CONTRIBUTING.md` - the most important rule up top (BLE protocol must
  be edited on both sides together, referencing the real bug this
  happened from), plus Android/firmware code conventions.
- `CHANGELOG.md` - every version from 0.1.0 through this point, matching
  `versionName` history, condensed from all the phase summary docs.
- `SECURITY.md` - scope, already-known limitations stated up front (XOR
  checksum isn't cryptographic, backups aren't encrypted, password hashing
  approach) so reports focus on genuinely new issues, reporting process.
- `CODE_OF_CONDUCT.md` - standard baseline.
- `.gitignore` - Android + Gradle + PlatformIO + OS/editor + secrets.
- `.github/ISSUE_TEMPLATE/` - bug report, feature request, and a
  project-specific **hardware/wiring issue** template.
- `.github/PULL_REQUEST_TEMPLATE.md` - includes a BLE-protocol-sync
  checklist so the two-sides-must-match rule is checked on every PR, not
  just documented in prose.
- `.github/CODEOWNERS` - placeholder GitHub username, **needs updating**
  to the real handle.
- `.github/workflows/android-ci.yml` - a suggested CI workflow (installs
  JDK + Android SDK, builds debug APK, runs lint) - addresses the earlier
  `sdk.dir` error by actually installing the SDK before Gradle runs. If a
  workflow already exists in the repo, compare rather than assume this
  replaces it.

## Known gap - please read before pushing
**`android/gradle/wrapper/gradle-wrapper.jar` (the binary wrapper jar)
could not be generated in this environment** (no access to
services.gradle.org to download a real Gradle distribution and generate
it). `gradlew`, `gradlew.bat`, and `gradle-wrapper.properties` (all plain
text) ARE included and correct. Before your first build or CI run:

```
cd android
gradle wrapper --gradle-version 8.7
```

(requires a local Gradle install), **or** simply open `android/` in
Android Studio, which detects the missing wrapper jar and regenerates it
automatically on sync. After that one-time step, commit the resulting
`gradle-wrapper.jar` and everything works normally from then on -
`./gradlew`, CI, all of it.
