# How to Use BikeOS

A guide for riders using the BikeOS app + hardware controller day to day.
(For build/setup instructions, see [`README.md`](./README.md) and
[`docs/18_WIRING_GUIDE.md`](./docs/18_WIRING_GUIDE.md).)

## First-time setup

1. **Install the app** and open it. You'll see a short welcome tour (swipe
   through, or tap Skip).
2. **Create your account** - name, username, email, and a password. This
   password is also your anti-theft alarm's quick-disarm code, so pick one
   you can type quickly.
3. On the same screen, fill in your **height, weight, and age** (used for
   calorie estimates) and your **bike's configuration** - bike name, type,
   wheel size, and how many front/rear gears it has. You can change any of
   this later from Settings.

From here on, opening the app always lands you on **Home**.

## Home screen

- A greeting up top (changes each time you open the app).
- A big **START** button in the middle - this is the only way into the
  cockpit/cluster view.
- Two cards at the bottom showing your total distance so far and a rough
  read on your riding style.
- The menu icon (☰, top-left) gets you to Calculator, Settings, About, and
  your Profile from anywhere in the app - not just from Home.

## Starting a ride

Tap **START**. The app will:
1. Try to connect to your BikeOS device over Bluetooth (grant the
   Bluetooth permission if asked). If it can't find your device within a
   few seconds, you'll get a "Continue without connection" option.
2. Play a short animated "engine start" sequence (can be turned off in
   Settings if you'd rather skip it).
3. Drop you into the cockpit.

**Entering the cockpit automatically starts tracking your ride.** There's
no separate "start ride" button inside the cockpit - just get in and go.

## The cockpit (cluster)

- Large speedometer in the center.
- Ride mode chips (Eco / Cruise / Sprint / Climb / Downhill) below it -
  tap to change, or use the **Mode** button on your handlebar.
- Light toggles (front / rear / body) - tap in-app, or use the physical
  **Light** button on your handlebar to cycle through them without
  touching the phone.
- Bottom cards: whichever of Distance / Calories / Cadence / Gear you've
  enabled in Settings > Appearance.
- If a call comes in, a banner appears with the caller's name - answer
  with the handlebar's **Gear Up** button, reject with **Gear Down**.
  (When no call is active, those same two buttons adjust your bike's
  gear instead.)
- If music is playing (and you've granted Notification access), a
  mini-player with play/pause/skip appears.
- Top-right: an **Exit** button. Tapping it (or using your phone's normal
  back gesture) ends and saves the ride, and takes you back to Home.

## Anti-theft alarm

Turn it on from Settings. Once armed, if the bike is moved or disturbed
while you're away, the buzzer sounds and the lights blink - and your
phone will show a password prompt the instant it's near your BikeOS
device, so you can disarm it quickly (handy at night). Enter your account
password to stop it.

## Ride reminders

If you haven't ridden yet today and it's around the time you usually do,
you may get a gentle notification. This goes quiet on its own if you
haven't opened the app in a couple of weeks - re-open the app any time to
pick it back up.

## Settings you can change any time

- **Theme**: dark/light, 12/24-hour clock, engine-start animation on/off.
- **Appearance**: which cockpit widgets are shown, and cluster colors for
  day vs. night (switches automatically).
- **Units**: kilometers or miles.
- **Bike Configuration**: wheel size, gear counts, current gear.
- **Backup**: export your data to a `.bop` file (useful before switching
  phones), or import one back in.
- **Erase all data**: wipes everything and takes you back through the
  welcome tour - use with care; export a backup first if you might want
  the data later.

## Troubleshooting

- **Can't connect to the bike?** Make sure the ESP32 is powered on, and
  that you've granted Bluetooth permission (Settings > Bluetooth
  Configuration > Scan & Connect).
- **Music widget not working?** It needs "Notification access" - tap the
  prompt inside the cockpit to jump straight to that system setting.
- **Speed/distance not showing while connected?** Double check your wheel
  size in Settings > Bike Configuration - that's what the app uses to
  convert your wheel's rotation speed into km/h.
