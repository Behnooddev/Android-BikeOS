#pragma once
// Phase J - alternate sensor hardware backends.
//
// The builder has some sensor units that don't match the originally
// assumed hardware/wiring. Each backend below is picked with a single
// compile-time #define so the rest of the firmware (sensors.h / motion.h
// function signatures) never has to care which physical chip/wiring is
// actually in use - only this file needs editing when swapping hardware.
//
// Uncomment exactly ONE option in each group below to select that
// backend. These are independent of each other - mix and match freely
// (e.g. HC-SR04 + library MPU6050 + AO Hall is a valid combination).

// ============================================================================
// Rear distance sensor
// ============================================================================
// VL53L1X  - I2C Time-of-Flight (original hardware, GPIO21/22 shared bus)
// HC_SR04  - ultrasonic trigger/echo, pulseIn() timing (NOT I2C)
#define BIKEOS_REAR_SENSOR_VL53L1X
// #define BIKEOS_REAR_SENSOR_HC_SR04

#if defined(BIKEOS_REAR_SENSOR_HC_SR04)
    // Trigger/Echo GPIOs for the HC-SR04. Pick free GPIOs - these are NOT
    // shared with the I2C bus (VL53L1X used GPIO21/22 for that; HC-SR04
    // doesn't touch I2C at all). Adjust to match actual wiring.
    #define HCSR04_TRIG_PIN 16
    #define HCSR04_ECHO_PIN 17
    // pulseIn() timeout - bounds how long a single measurement can block.
    // ~4m max range at ~343m/s round trip -> ~23ms; 30ms gives margin.
    #define HCSR04_PULSEIN_TIMEOUT_US 30000UL
#endif

// ============================================================================
// Motion / IMU (MPU6050) reading path
// ============================================================================
// LIBRARY       - Adafruit MPU6050 library, calibrated/scaled output (original)
// RAW_AVERAGING - direct raw register reads + rolling average, bypassing the
//                 library's calibration - more robust against noisy/
//                 mis-calibrated counterfeit MPU6050 clones.
#define BIKEOS_MPU6050_LIBRARY
// #define BIKEOS_MPU6050_RAW_AVERAGING

#if defined(BIKEOS_MPU6050_RAW_AVERAGING)
    // Number of raw samples averaged into each reading - higher smooths
    // more but reacts slower to a genuine theft-jostle event.
    #define MPU6050_RAW_AVERAGE_WINDOW 8
#endif

// ============================================================================
// Wheel/cadence Hall sensor interface
// ============================================================================
// DIGITAL - digital "DO" pin, interrupt-driven pulse counting (original)
// ANALOG  - analog "AO" pin, polled + threshold-compared (some modules only
//           expose AO, or expose both but AO is more reliable on certain
//           counterfeit boards)
#define BIKEOS_HALL_MODE_DIGITAL
// #define BIKEOS_HALL_MODE_ANALOG

// Active-level polarity - independent of DIGITAL vs ANALOG above.
// LOW  = magnet present pulls the pin LOW (original assumption)
// HIGH = magnet present pulls/reads the pin HIGH (some modules are wired
//        or behave the opposite way - flip this if pulses aren't
//        registering, or are registering doubled/inverted, on real hardware)
#define BIKEOS_HALL_ACTIVE_LOW
// #define BIKEOS_HALL_ACTIVE_HIGH

#if defined(BIKEOS_HALL_MODE_ANALOG)
    // ESP32 ADC1 pins only (ADC2 conflicts with WiFi/BLE radio use) - GPIO32/33/34/35/36/39
    // are all valid ADC1 choices; picked to avoid the existing button pins
    // (GPIO32/33/25/26 per docs/18_WIRING_GUIDE.md).
    #define WHEEL_HALL_AO_PIN 34
    #define CADENCE_HALL_AO_PIN 35
    // ESP32 analogRead() default resolution is 12-bit (0-4095). A magnet's
    // AO output swings close to full-scale in one direction at close
    // range - this threshold is a starting point, tune against a real
    // module with a multimeter/serial-print during bring-up.
    #define HALL_AO_THRESHOLD 2048
#endif
