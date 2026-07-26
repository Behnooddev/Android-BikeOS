# Security Policy

BikeOS is a proprietary hobbyist/personal project (see
[`LICENSE`](./LICENSE)), but it does handle sensitive data (account
password hash, BLE communication with hardware that controls physical
outputs) and we take reports of real security issues seriously.

## Scope

Things we consider in scope:
- Vulnerabilities in the Android app (`android/`) - e.g. insecure storage,
  authentication/authorization bypass, injection issues.
- Vulnerabilities in the ESP32 firmware (`firmware/`) - e.g. BLE command
  injection, unauthenticated control of lights/alarm, buffer overflows in
  packet parsing.
- Issues in the BLE protocol itself (`firmware/src/protocol/bikeos_protocol.h`
  / `android/.../data/ble/BlePacket.kt`) that would let an unauthorized
  nearby device read data or send commands.

Known, already-documented limitations (not new reports):
- The BLE packet checksum is a simple XOR, not a cryptographic integrity
  check - adequate for catching corruption, not for authentication. BLE
  pairing/bonding hardening is tracked as future work (see the
  architecture review in `docs/`).
- Local `.bop` backup files are not encrypted, despite containing a
  hashed+salted password. Also tracked as future work.
- The signup password is hashed with salted, iterated SHA-256 (not
  bcrypt/Argon2) - a deliberate simplification since there's no
  server-side account system yet (see `PasswordHasher.kt`'s kdoc). This is
  a known, documented trade-off, not an oversight.

## Reporting a Vulnerability

If you find a security issue, please report it privately rather than
opening a public GitHub issue:

- Open a private security advisory via GitHub's "Report a vulnerability"
  feature on this repository (Security tab), if enabled, **or**
- Contact the maintainer directly through a private channel.

Please include:
- A description of the issue and its potential impact.
- Steps to reproduce (a proof of concept, if you have one).
- The affected version/commit.

We'll acknowledge reports as quickly as we can and work on a fix before
any public disclosure. Please give us reasonable time to address the
issue before disclosing it publicly.

## Supported Versions

This project does not yet have a formal release/support cadence (see
`CHANGELOG.md` for version history) - security fixes are applied to the
latest `main` branch.
