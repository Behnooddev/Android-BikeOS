#pragma once
// BikeOS device identity constants.
// Real per-unit Device ID generation (from efuse/MAC) is still a TODO -
// BIKEOS_DEVICE_NAME is a fixed placeholder until that lands.

#define BIKEOS_DEVICE_NAME     "BikeOS Device"
#define BIKEOS_FIRMWARE_VERSION "0.4.0"
#define BIKEOS_HARDWARE_VERSION "0.1.0"
// 1.1: Phase 4 changed the Sensor Data payload from firmware-computed
// speed/distance to raw wheelRpm + a newly-wired rearDistanceMm - a
// breaking wire-format change from protocol 1.0. See ble_service.cpp.
#define BIKEOS_PROTOCOL_VERSION "1.1"
