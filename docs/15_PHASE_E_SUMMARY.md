# Phase E Summary - Ride Reminder Notifications

## How it works
`ReminderWorker` (WorkManager, `@HiltWorker`) runs every 30 minutes and is
almost always a no-op - it only shows a notification when ALL of these
hold:
1. Settings > "Reminder notifications" is on.
2. The app has been opened within the last 15 days - per the spec, if it's
   been longer than that, stay quiet until the user opens the app again
   (don't nag someone who's stopped using it).
3. The user hasn't already started a ride today (`AppState.lastRideStartEpochDay`).
4. Current time is within +/-30 minutes of the user's LEARNED average
   ride-start time (`AppState.avgRideStartMinuteOfDay` - a running mean
   updated every time Start Ride is pressed, via
   `AppStateRepository.recordRideStart`, now actually wired into
   `DashboardViewModel.startRide()`).
5. POST_NOTIFICATIONS is granted (API 33+ only).

## New pieces
- `NotificationHelper` - creates the notification channel, shows the
  reminder.
- `ReminderScheduler` - enqueues the periodic work exactly once
  (`ExistingPeriodicWorkPolicy.KEEP`), called from
  `BikeOSApplication.onCreate()`.
- `BikeOSApplication` now implements `Configuration.Provider` so
  `ReminderWorker` can be constructor-injected via Hilt instead of needing
  a no-arg constructor - the standard Hilt+WorkManager integration pattern.
- POST_NOTIFICATIONS requested once from `HomeScreen` (API 33+ only) -
  declining doesn't break anything else, `ReminderWorker` re-checks the
  permission itself before ever showing anything.

## Bug fixed while wiring this up
`AppStateRepository.recordRideStart()` existed since Phase A but was never
actually called anywhere - the learned average ride-start time would have
stayed at its default (8:00 AM) forever. Now called from
`DashboardViewModel.startRide()`.

## Known simplifications
- 30-minute check interval means the reminder could theoretically fire up
  to ~30 minutes after the exact learned time, not precisely on it -
  fine for "around your usual time", not appropriate if exact-minute
  timing ever matters for a future feature.
- No snooze/dismiss-tracking - if the user dismisses the notification
  without riding, nothing stops another one from firing at the next
  30-minute check within the same window (in practice the window is only
  60 minutes wide total, so this means at most 1-2 notifications per day,
  not a spam risk).

## How to verify
Grant notification permission, ride a few times (or manually adjust
`avgRideStartMinuteOfDay` via a debug backup/import round-trip) so the
learned time is close to "now", then wait for the next 30-minute
WorkManager tick - a notification should appear once, and not again after
riding that day. Toggle the Settings switch off and confirm it stays
silent even during the right time window.
