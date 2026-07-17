#pragma once
// BLE module - Phase 4 (real hardware wired in).
//
// Implements the full GATT tree: Device Information Service (read once at
// connect), Sensor Data Service (notify - real Hall-derived wheel/cadence
// RPM + real INA219 battery % when available; also carries Button Event
// packets for the Mode/Gear-Up/Gear-Down buttons, since Android needs to
// hear about those too), and Control Service (write - light commands now
// really drive the MOSFET outputs via controls.cpp).

namespace bikeos::ble {
    void init();   // starts BLE stack, GATT services, and advertising
    void tick();   // per-loop hook: drains pending button events, sends a Sensor Data notification on a timer while connected
}
