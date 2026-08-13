# UI Visual Overhaul - synced against builder-supplied cockpit designs

Not a phase. The builder supplied two exported design references (each a
`code.html` + `screen.png` + `DESIGN.md` "Stitch" bundle) - a landscape
cockpit layout ("Precision Glassmorphism" / VoidRoot BikeOS Precision) and
a portrait home-screen layout ("BikeOS - Main Dashboard") - and asked for
the existing Dashboard and Home screens to be visually reskinned to match,
with any small missing control added, and nothing else touched: no new
phase, no logic/state changes, no ViewModel changes.

## What was compared

- `stitch_bikeos_horizon_cockpit` -> maps to `DashboardScreen.kt` (the
  landscape ride cluster).
- `stitch_bikeos_smart_cockpit_interface` -> maps to `HomeScreen.kt` (the
  portrait landing screen).

The existing app was already close in spirit (dark glassmorphism, cyan
accent, a `GlassCard` component, a spring-animated speed gauge) since it
was designed independently toward a similar "premium cockpit" direction -
this pass is a structural/detail sync, not a rebuild from zero.

## Dashboard changes (`presentation/dashboard/`)

- **Layout**: was a centered `Row` (gauge + a narrow column holding mode
  chips + light chips) with a separate bottom info row. Rebuilt as a real
  3-column cockpit body per the reference: left = stat cards
  (Distance/Calories/Cadence/Gear) stacked vertically instead of along
  the bottom, center = gauge + mode selector + music widget, right = a
  new vertical column of icon-card light toggles.
- `components/LightControlRow.kt` - was a horizontal row of text pill
  chips ("☀ Front" etc). Rebuilt as a vertical stack of square glass
  cards, each with a circular icon (Lightbulb/WbIncandescent/
  DirectionsBike for Front/Rear/Body) that fills + glows amber when on,
  matching the reference's circular icon-button control column. Same
  props/signature, so the `DashboardScreen.kt` call site only needed to
  move, not change its arguments.
- `components/RideModeSelector.kt` - swapped each mode's Unicode glyph
  for a real `ImageVector` (Cloud/RadioButtonChecked/Bolt/Terrain/
  ArrowDownward for Eco/Cruise/Sprint/Climb/Downhill) via a small
  `modeIcon()` mapper, kept next to the label inside the same pill.
- `components/SpeedGauge.kt` - thinned the arc (18dp -> 6dp stroke) and
  dropped the extra double-glow bands in favor of one soft glow pass +
  one gradient line, matching the reference's "thin, high-precision arc"
  language instead of the old thick liquid-fill band. Spring animation
  logic untouched.
- `components/MusicWidget.kt` - swapped emoji transport controls (⏮ ⏸ ▶
  ⏭) for real Material icons (SkipPrevious/Pause/PlayArrow/SkipNext), and
  the "no album art" case now shows a circular MusicNote icon placeholder
  instead of just collapsing.
- `DashboardScreen.kt`'s `TopStatusRow` - battery reading and Exit button
  gained icons (`BatteryFull`, `Close`); the old inline `BottomInfoRow`
  was replaced by a new `StatColumn` (same widget-key gating, same data,
  just a `Column` instead of a `Row` so it can live on the left).

## Home changes (`presentation/menu/home/HomeScreen.kt`)

- Added a decorative distance ring (thin circular progress arc + big
  number + "km" label) above the greeting - purely visual, reuses the
  same `uiState.totalDistanceKm` already shown in the Total Distance
  card below it, no new state.
- Start button restyled to "START RIDE" with a `PlayArrow` icon,
  otherwise the same gradient pill + glow + navigation call as before.
- Total Distance / Riding Style cards gained small leading icons (Route /
  DirectionsBike).

## `presentation/common/BikeOSMenuScaffold.kt`

Gained an optional `actions: @Composable (RowScope.() -> Unit)? = null`
parameter (trailing-icon slot on the header row, unused by any screen
yet - a placeholder `Box(40.dp)` keeps the title centered when absent)
and a bold gradient-tinted title, matching the reference top bars'
branded look. Backward compatible - every existing call site uses the
trailing-lambda-for-`content` form, which still resolves correctly with
the new parameter defaulted in the middle.

## Explicitly NOT done

- No BLE/connection-status icon was added to Home's top bar, even though
  the smart-cockpit reference shows one - Home doesn't hold any
  connection state (only Dashboard does, after Start is pressed), so a
  decorative-only signal icon there would misrepresent status. Flag this
  if a real "is the phone paired with a bike controller at all" concept
  is wanted later - that would be actual new scope, not a visual sync.
- Ride-mode set (Eco/Cruise/Sprint/Climb/Downhill) unchanged - the
  reference only shows 3, the app already had 5; kept all 5.

## Verification status

Same standing limitation as the rest of the project: no Android SDK in
this sandbox, so nothing here has been through a real Gradle build.
Per-file brace/paren balance was checked statically. The main compile
risk worth flagging: `Icons.Filled.Route`, `WbIncandescent`, `Terrain`,
and `RadioButtonChecked` are being used from `material-icons-extended`
for the first time in this codebase (existing code only used a handful
of icons like `Menu`) - they're standard, long-existing glyphs in that
artifact, but haven't been build-confirmed against this project's exact
Compose BOM (`2024.06.00`) yet. If the build reports any of them
unresolved, that's a one-line icon swap, not a design problem.
