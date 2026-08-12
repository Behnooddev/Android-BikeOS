# BikeOS - ESP32 Wiring Guide (Complete - Phase D hardware)

Matches the GPIO constants defined in the firmware. If you rewire
differently, update the `#define` pins in `sensors.cpp` / `controls.cpp` /
`alarm.cpp` to match - the diagram and the code must never drift apart.

## Full pin table

| ESP32 Pin | Connects to | Notes |
|---|---|---|
| 3V3 | VL53L1X VIN, INA219 VIN, MPU6050 VIN, both Hall sensor VCC | Shared 3.3V rail. Confirm each breakout is 3.3V-safe (most are). |
| GND | All module GNDs, all button "other leg"s, MOSFET sources, buzzer GND | Use a shared ground rail/bus, not separate isolated grounds. |
| GPIO21 | VL53L1X SDA, INA219 SDA, MPU6050 SDA | I2C bus, shared - all three sit on the same SDA/SCL lines. |
| GPIO22 | VL53L1X SCL, INA219 SCL, MPU6050 SCL | Same I2C bus as above. |
| GPIO4 | Wheel Hall sensor OUT | Interrupt pin, `INPUT_PULLUP`, active LOW (magnet present = LOW). |
| GPIO5 | Cadence Hall sensor OUT | Same as above, for the crank sensor. |
| GPIO13 | Front light MOSFET gate | Through a ~220 ohm series resistor; add a 10k ohm pulldown gate-to-GND. |
| GPIO14 | Rear light MOSFET gate | Same as above. |
| GPIO27 | Body light MOSFET gate | Same as above. |
| GPIO23 | Buzzer (anti-theft alarm) | Small passive piezo buzzer under ~20mA can go direct off the pin; anything louder needs a transistor switch, same pattern as the lights. |
| GPIO32 | Light button | `INPUT_PULLUP`; button's other leg to GND. Local toggle, cycles lights. |
| GPIO33 | Mode button | `INPUT_PULLUP`; other leg to GND. Sends a BLE event to the app. |
| GPIO25 | Gear Up button | `INPUT_PULLUP`; other leg to GND. Sends a BLE event to the app (or answers a call, if one's ringing). |
| GPIO26 | Gear Down button | `INPUT_PULLUP`; other leg to GND. Sends a BLE event to the app (or rejects a call, if one's ringing). |

## I2C bus (VL53L1X + INA219 + MPU6050, all sharing GPIO21/22)

Three devices on the same two wires - normal I2C behavior as long as each
has a distinct address (they do, by default: VL53L1X `0x29`, INA219
`0x40`, MPU6050 `0x68`). Wire all three in parallel (same 3.3V rail, same
GND, same SDA to GPIO21, same SCL to GPIO22) - don't daisy-chain through
the sensors themselves.

If readings are erratic from any of the three, add a single shared 4.7k
ohm pull-up from SDA to 3.3V and another from SCL to 3.3V (many breakouts
already have these on-board - check before doubling up; extra pull-ups
just stiffen the bus, they won't break anything).

## MOSFET wiring detail (per light)

Each light is switched by an N-channel **logic-level** MOSFET (e.g.
IRLZ44N or AO3400 - must be logic-level since GPIO only outputs 3.3V):

```
GPIO(13/14/27) --[220ohm]-- Gate
Gate --[10k ohm]-- GND         (pulldown: keeps light OFF during boot,
                                 before the GPIO is configured as OUTPUT)
Source -> GND (shared with ESP32 GND)
Drain -> Light(-)
Light(+) -> Battery+ (direct from battery, NOT from a GPIO or 3.3V rail -
                       GPIOs cannot supply enough current for real lights)
```

**Why the pulldown resistor matters**: without it, the MOSFET gate floats
during the brief window between power-on and `pinMode(..., OUTPUT)`
executing in `setup()`, which can cause a light to flicker on briefly at
boot. The 10k ohm pulldown holds it firmly OFF until firmware takes control.

## Buzzer wiring

```
GPIO23 -> Buzzer(+)
Buzzer(-) -> GND
```

If your buzzer is loud/high-current (an actual alarm-grade siren rather
than a small PCB piezo), don't drive it directly off the GPIO - use the
same MOSFET pattern as the lights (GPIO -> gate through a resistor,
buzzer+ to battery+, buzzer- to MOSFET drain, MOSFET source to GND).

## Buttons

All four buttons use the ESP32's internal pull-up (`INPUT_PULLUP` in
firmware) - a simple momentary push-button between the GPIO pin and GND,
no external resistor required.

## Magnets (Hall sensors - not wired, positioned)

- **Wheel**: one small neodymium magnet mounted on a spoke, with the Hall
  sensor mounted on the fork/frame so the magnet passes within a few mm
  of the sensor face once per wheel rotation (same idea as a standard bike
  computer speed sensor).
- **Crank**: one magnet on the crank arm (or pedal spindle), sensor
  mounted on the frame near the bottom bracket, same clearance idea.
- If pulses seem to be missed at low speed, get the magnet closer to the
  sensor face (a few mm gap is typical) rather than changing firmware
  thresholds first.

## Power

- ESP32 dev boards typically accept 5V on VIN (onboard regulator down to
  3.3V) or can be powered directly on 3V3 if your board allows it - check
  your specific board's documentation.
- A single-cell Li-ion/LiPo (3.7V nominal) through a TP4056 charge/protect
  module is a common choice - the TP4056's output then either goes to a
  5V boost converter (if your ESP32 board wants 5V on VIN) or straight to
  3V3 on boards that accept it directly.
- Lights and the buzzer (if high-current) are powered directly from
  battery+, switched by MOSFETs - never route that current through the
  ESP32 or a GPIO pin.
- INA219 sits in series between the battery and the rest of the load so it
  can measure current draw - check the specific breakout's silkscreen for
  which side is "VIN+"/battery vs "VIN-"/load.

## Alternate hardware (Phase J)

The pin table and assembly steps above are for the **confirmed, original
hardware** (VL53L1X, library-driven MPU6050, digital active-LOW Hall
modules) and remain the default - `firmware/src/config/sensor_backend_config.h`
ships with those as the active `#define`s. If you're using different
sensor units, flip the relevant switch(es) in that file **and** update
your wiring per below. See `docs/21_PHASE_J_SUMMARY.md` for the full
write-up of why/how these alternates were built.

### HC-SR04 (instead of VL53L1X, rear distance)
- **VCC -> 5V.** Unlike the VL53L1X, HC-SR04 breakouts generally need 5V
  to work reliably - check your specific module's silkscreen, but don't
  assume 3.3V is enough.
- **GND -> GND** (shared with everything else).
- **TRIG -> a free GPIO** (default in `sensor_backend_config.h`: GPIO16),
  driven directly by the ESP32 at 3.3V logic - HC-SR04 TRIG accepts 3.3V
  logic fine.
- **ECHO -> level-shifted down to a free GPIO** (default: GPIO17).
  **This is the wiring hazard to get right**: HC-SR04's ECHO output swings
  to 5V, which can damage an ESP32 GPIO over time. Use a simple resistor
  voltage divider (e.g. 1k/2k) or a proper logic level shifter between
  ECHO and the ESP32 pin - do not wire ECHO directly to the ESP32.
- Does **not** use the I2C bus at all - fully independent wiring from
  VL53L1X/INA219/MPU6050.

### MPU6050 raw-averaging mode
No wiring change - same I2C bus (GPIO21/22), same chip. Only the
firmware's *software* reading path changes (`BIKEOS_MPU6050_RAW_AVERAGING`
in the config file). Worth double-checking the chip's I2C address is
still the default `0x68` (AD0 pin tied low/unconnected) if switching to a
different physical unit.

### Hall sensor AO (analog) mode
- Wiring is otherwise identical to the digital DO setup (VCC -> 3.3V,
  GND -> GND), except the signal wire connects to the module's **AO** pin
  instead of **DO**.
- **AO must go to an ESP32 ADC1-capable GPIO** (ADC2 pins conflict with
  WiFi/BLE radio use and will misbehave). Defaults in
  `sensor_backend_config.h`: GPIO34 (wheel), GPIO35 (cadence) - both
  ADC1, both free of the existing button/light pins.
- `HALL_AO_THRESHOLD` in the config file is a starting guess (half of the
  ESP32's 12-bit ADC range) - **tune it against your real module** (a
  multimeter on the AO pin, or a temporary `Serial.println(analogRead(...))`
  during bring-up) before trusting RPM readings from it.
- Active-HIGH vs active-LOW polarity (`BIKEOS_HALL_ACTIVE_HIGH`/`_LOW`) is
  a separate switch in the same file and applies to AO mode the same way
  it applies to DO mode - flip it if pulses read inverted.

## Assembly order (recommended, so you can test incrementally)

1. ESP32 alone - flash firmware, confirm serial log boots and BLE
   advertises (no sensors needed for this).
2. I2C bus - wire VL53L1X, INA219, MPU6050 one at a time, confirming each
   one's "ready" log line before adding the next.
3. Hall sensors - wire, spin each by hand, confirm RPM shows up over BLE
   in the app.
4. Lights + buttons - wire, confirm the physical Light button cycles
   correctly and the app's light toggles work independently.
5. Buzzer - wire last, arm the alarm from the app and test-trigger it
   (spin the wheel or shake the bike) to confirm the full trigger -> buzz
   -> app dialog -> disarm loop works end to end.

## Assembly recommendation for v1

Don't go straight to a custom PCB - solder this onto a perfboard/protoboard
first. It's faster to fix a wiring mistake with a soldering iron and
patience than to redesign and reorder a PCB. Once everything above is
confirmed working, that perfboard layout is a solid reference for a future
PCB if you want one.

## Cross-reference

See `firmware/src/sensors/sensors.cpp`, `firmware/src/controls/controls.cpp`,
`firmware/src/power/power.cpp`, `firmware/src/motion/motion.cpp`,
`firmware/src/alarm/alarm.cpp` for the exact `#define` pin constants this
table is derived from.
