# Phase I Summary - UI Overhaul, App Icon, and 2 Bug Fixes

## Scope
Builder asked for: a "modern, premium, Apple x Tesla" dark-mode
redesign; a cluster (Dashboard) that reads like a modern car's
multimedia system crossed with BMW's cluster - theme/feel only, not an
actual multimedia player; an app that "feels like a million-dollar
product" on open; the provided app icon wired in; and 2 concrete bugs
fixed. This pass covers the design-system-level changes and the two
bugs - a full pixel-by-pixel redesign of all 15+ screens wasn't
attempted in one pass (see "Not done" at the bottom for what's left).

## Bug fixes

### Bug 1: keyboard covers the lower Signup fields
**Root cause**: `MainActivity` uses `enableEdgeToEdge()`, which means
`windowSoftInputMode="adjustResize"` alone doesn't reliably push content
up anymore (that flag is largely superseded once
`decorFitsSystemWindows` is false) - the actual fix on edge-to-edge is
each scrollable container's own `Modifier.imePadding()`.

**Fix**: added `.imePadding()` to every screen with text fields -
`SignupScreen`, `AccountScreen`, `SettingsScreen`, `CalculatorScreen` -
plus `android:windowSoftInputMode="adjustResize"` on the Activity in
`AndroidManifest.xml` as a harmless belt-and-suspenders for any
older/OEM edge case.

### Bug 2: menu button drifts off-screen on some phone sizes
**Root cause**: `BikeOSMenuScaffold` (the shared top-row + drawer wrapper
used by Home/Calculator/Settings/About/Profile) had no status-bar inset
handling at all - since the app draws edge-to-edge, the menu button/title
row was drawing directly underneath the status bar, and how far
"underneath" varies by device (status bar height differs by screen
size, notch/cutout, OEM skin) - which is exactly the "sometimes the
screen gets too big and the menu button goes up there" symptom
described.

**Fix**: added `Modifier.statusBarsPadding()` to `BikeOSMenuScaffold`'s
outer Column. This reads the real `WindowInsets` at runtime rather than
a hardcoded dp offset tuned for one test device - the correct way to make
something "responsive to screen size", as opposed to literally scaling
the whole UI by screen dimensions (which would be the wrong fix and
would make text/touch-targets inconsistently sized across devices).
Also added `statusBarsPadding()`/`navigationBarsPadding()` to
`OnboardingScreen`'s Skip button and bottom controls, and
`navigationBarsPadding()` to Signup's submit button area, for the same
underlying reason (found while fixing the reported bug, same root
cause class).

## App icon
The builder's provided icon (a bicycle wheel motif, dark rounded-square)
was processed into a full icon set:
- Legacy square + round launcher icons at all 5 mipmap densities
  (mdpi-xxxhdpi), flattened onto the app's own background color so
  there's no visible seam between the icon and its background.
- Adaptive icon (API 26+) foreground PNGs at all 5 densities, inset to
  ~62% of the canvas so the system's mask (circle/squircle/rounded-
  square, varies by OEM) doesn't clip the bike glyph - paired with a
  solid-color background layer matching the app's theme.
- Old placeholder vector icons (`ic_launcher_foreground.xml`,
  `ic_launcher_legacy.xml`) deleted.
- A 512x512 Play Store hi-res copy saved to `playstore_assets/` (not
  part of the app itself, just kept alongside for whenever a store
  listing is needed).

## Design system refresh ("Apple x Tesla" direction)
- **`core/theme/Color.kt`**: background shifted to a true near-black,
  blue-tinted tone (`#06070A`, was `#0A0E14`) for more OLED-style depth;
  primary accent shifted from a neon/gaming cyan (`#00E5FF`) to a
  restrained "signal blue" (`#3E8EFF`, closer to Apple's systemBlue /
  Tesla's interactive-element blue); secondary accent refined to a
  deeper indigo-violet (`#8B5CF6`); text/status colors refined to match.
  Names unchanged (`BikePrimary`, `BikeBackground`, etc.) so every
  existing screen that already reads these tokens picks up the new
  palette automatically - no per-screen color changes needed.
- **`res/values/colors.xml`** (new file): mirrors `BikeBackground` for
  the places XML resources need a color reference (splash theme,
  adaptive icon background) that can't read Kotlin constants directly.
- **`core/theme/Type.kt`**: added tight negative letter-spacing to the
  big display number (reads more like an instrument-cluster readout)
  and a touch of positive tracking on small labels (the "spaced-out
  caps" look premium automotive/Apple UIs use for secondary text).
- **`core/common/GlassCard.kt`**: added a top-edge-brighter border
  gradient on top of the existing uniform border - real glass/acrylic
  catches more light along its top edge, and that asymmetry is what
  reads as "glass" rather than "flat card with a border" at a glance.
- **`presentation/dashboard/components/SpeedGauge.kt`**: added
  instrument-cluster-style tick marks around the arc (12 graduations,
  every 4th drawn longer/brighter as a "major" tick) - the BMW-cluster
  reference point specifically was "a gauge reads against fixed
  graduations", not just a bare progress ring - and switched the
  digital speed readout to a monospace font, which is what gives
  premium automotive/aviation instrument numbers their "precision tool"
  look versus a generic proportional UI font.
- **Splash screen** (new): added `androidx.core:core-splashscreen`,
  a `Theme.BikeOS.Splash` theme (real launcher icon on the app's own
  background color, not a blank system flash), and a custom exit
  animation in `MainActivity` (fade + slight scale-up, closer to how
  iOS/premium apps transition off their launch screen than the default
  hard cut) - directly aimed at the "feels like a million-dollar
  product when it opens" ask.

## Not done in this pass
This was a design-system-level refresh (palette, typography, the shared
glass card, the splash screen, the cluster's main gauge) plus the 2 bugs
- not a screen-by-screen rebuild. Specifically NOT touched:
- Every other screen's individual layout/spacing (Home, Settings,
  About, Profile, Calculator) - they inherit the new colors/typography
  automatically (all read the shared tokens), but their layouts are
  unchanged from before this pass.
- The rest of the Dashboard/cluster's widgets (light toggles, ride mode
  selector, bottom info cards) beyond what GlassCard's refresh already
  gives them - could get more BMW/Tesla-multimedia-specific treatment
  (e.g. a bottom tab-bar-style widget switcher, more explicit
  "sections" like a real car multimedia home screen) as a follow-up.
- No new app icon adaptive-icon monochrome variant (Android 13+ themed
  icons) was created - only the standard adaptive icon.

## Not build/runtime-verified
Same sandbox limitation as every other phase: no Gradle/Android runtime
here to actually render any of this. Static checks only (brace/paren
balance on every edited `.kt` file, XML well-formedness on every edited
resource file via `xml.etree.ElementTree`). The splash screen, icon
rendering across real device shapes, and the new color palette's actual
on-screen contrast/legibility all need a real device or emulator to
confirm - this is a genuinely higher-risk pass to ship unverified than
most previous phases given how much of it is visual/subjective, so
budget real look-and-feel time before considering this "done" rather
than just "compiles".
