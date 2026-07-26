# BikeOS - ESP32 Wiring Guide (Phase 4)

Matches the GPIO constants defined in the firmware. If you rewire
differently, update the `#define` pins in `sensors.cpp` / `controls.cpp`
to match - the diagram and the code must never drift apart.

## Full pin table

| ESP32 Pin | Connects to | Notes |
|---|---|---|
| 3V3 | VL53L1X VIN, INA219 VIN, both Hall sensor VCC | Shared 3.3V rail. Confirm each breakout is 3.3V-safe (most are). |
| GND | All module GNDs, all button "other leg"s, MOSFET sources | Use a shared ground rail/bus, not separate isolated grounds. |
| GPIO21 | VL53L1X SDA, INA219 SDA | I2C bus, shared - both sensors sit on the same SDA/SCL lines. |
| GPIO22 | VL53L1X SCL, INA219 SCL | Same I2C bus as above. |
| GPIO4 | Wheel Hall sensor OUT | Interrupt pin, `INPUT_PULLUP`, active LOW (magnet present = LOW). |
| GPIO5 | Cadence Hall sensor OUT | Same as above, for the crank sensor. |
| GPIO13 | Front light MOSFET gate | Through a ~220Ω series resistor; add a 10kΩ pulldown gate-to-GND. |
| GPIO14 | Rear light MOSFET gate | Same as above. |
| GPIO27 | Body light MOSFET gate | Same as above. |
| GPIO32 | Light button | `INPUT_PULLUP`; button's other leg to GND. Local toggle, cycles lights. |
| GPIO33 | Mode button | `INPUT_PULLUP`; other leg to GND. Sends a BLE event to the app. |
| GPIO25 | Gear Up button | `INPUT_PULLUP`; other leg to GND. Sends a BLE event to the app. |
| GPIO26 | Gear Down button | `INPUT_PULLUP`; other leg to GND. Sends a BLE event to the app. |

## MOSFET wiring detail (per light)

Each light is switched by an N-channel **logic-level** MOSFET (e.g.
IRLZ44N or AO3400 - must be logic-level since GPIO only outputs 3.3V):

```
GPIO(13/14/27) --[220Ω]-- Gate
Gate --[10kΩ]-- GND            (pulldown: keeps light OFF during boot,
                                 before the GPIO is configured as OUTPUT)
Source -> GND (shared with ESP32 GND)
Drain -> Light(-)
Light(+) -> Battery+ (direct from battery, NOT from a GPIO or 3.3V rail -
                       GPIOs cannot supply enough current for real lights)
```

**Why the pulldown resistor matters**: without it, the MOSFET gate floats
during the brief window between power-on and `pinMode(..., OUTPUT)`
executing in `setup()`, which can cause a light to flicker on briefly at
boot. The 10kΩ pulldown holds it firmly OFF until firmware takes control.

## I2C bus (VL53L1X + INA219 sharing GPIO21/22)

Both sensors sit on the same two wires (SDA/SCL) - this is normal I2C
behavior, not a conflict, as long as they have different I2C addresses
(they do, by default: VL53L1X is `0x29`, INA219 is `0x40`). Wire them in
parallel (both VIN to the same 3.3V rail, both GND to the same ground, both
SDA to GPIO21, both SCL to GPIO22) - do not daisy-chain through the sensors
themselves.

If you see erratic readings from either sensor, add 4.7kΩ pull-up
resistors from SDA and SCL to 3.3V (many breakout boards already include
these on-board - check before adding a second set, doubling up makes the
bus stiffer than necessary but won't break anything).

## Buttons

All four buttons use the ESP32's internal pull-up (`INPUT_PULLUP` in
firmware) - you only need a simple momentary push-button between the GPIO
pin and GND, no external resistor required.

## Power

- ESP32 dev boards typically accept 5V on VIN (with an onboard regulator
  down to 3.3V) or can be powered directly on 3V3 if your board allows it -
  check your specific board's documentation.
- A single-cell Li-ion/LiPo (3.7V nominal) through a TP4056 charge/protect
  module is a common choice - the TP4056's output then either goes to a
  5V boost converter (if your ESP32 board wants 5V on VIN) or can go
  straight to 3V3 on boards that accept it directly.
- Lights are powered directly from battery+, switched by the MOSFETs -
  never route light current through the ESP32 or a GPIO pin.

## Assembly recommendation for v1

Don't go straight to a custom PCB - solder this onto a perfboard/protoboard
first. It's faster to fix a wiring mistake with a soldering iron and
patience than to redesign and reorder a PCB. Once the wiring is confirmed
working end-to-end (all sensors reading correctly, all lights/buttons
responding), that perfboard layout is a solid reference for a future PCB
if you want one - happy to help translate it into a schematic-capture tool
like KiCad at that point, but hand-drawn Gerber files aren't something I
can reliably produce as text.

## Cross-reference

See `BikeOS_Wiring_Diagram.svg` for the visual block diagram, and
`firmware/src/sensors/sensors.cpp`, `firmware/src/controls/controls.cpp`,
`firmware/src/power/power.cpp` for the exact `#define` pin constants this
table is derived from.
