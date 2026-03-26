# Partial Implementation Audit for Features A, B, C

## Scope Note

The latest commit on `main` (`3e014893`) does **not** contain feature work for A, B, or C. It only updates `docs/user_reported_issues.md`.

The salvageable work is in the current uncommitted diff against `HEAD`, and it matches the attached transcript of the failed autonomous batch.

## Partially Implemented Features

### A — Settings Backup and Restore

Status: partially implemented in settings UI and fragment logic.

Work started:

- `SettingsSystemFragment.kt` adds backup and restore launchers using `ActivityResultContracts.CreateDocument` and `OpenDocument`.
- Backup serialization logic was added: preferences are exported to human-readable JSON with a `version` field and typed values.
- Restore logic was added: JSON is parsed back into `SharedPreferences`, with existing restorable keys cleared before reapplying values.
- Restore filtering was started to exclude device-specific widget placement data such as widget ids, widget ordering, and widget size/full-width keys.
- `fragment_settings_system.xml` adds a new `Backup & Restore` section with `Backup Settings` and `Restore Settings` buttons plus explanatory text.
- `strings.xml` adds success, failure, and invalid-file strings for backup/restore.

Transcript evidence:

- The prior run explicitly planned JSON backup via `ACTION_CREATE_DOCUMENT` and restore via `ACTION_OPEN_DOCUMENT`.
- The transcript states that `SettingsSystemFragment.kt` was patched with backup/restore flow and that diagnostics were clean.
- There is no transcript evidence that the backup/restore flow was exercised on-device before the run ended.

### B — Access Settings via Long-Press on Home Screen

Status: partially implemented in launcher gesture handling, with only partial runtime validation captured.

Work started:

- `MainActivity.kt` adds home long-press state, timeout handling, touch-slop cancellation, and a dedicated `openLauncherSettings()` helper.
- Long-press handling was wired in three places:
  - root layout long-press
  - empty widget area long-press
  - empty-space detection inside the `ListView` via `handleHomeEmptySpaceTouch()`
- The new logic explicitly avoids firing while the launcher is expanded, while widget edit mode is active, or while search is visible.
- Existing app-row long-press behavior was preserved, so app actions should still win when the press lands on an app item.

Transcript evidence:

- The prior run describes the exact empty-space long-press design and then patches `MainActivity.kt` accordingly.
- Emulator testing reached this feature: one long-press hit an app row and opened the existing app actions sheet, which confirms gesture routing was considered and partly validated.
- The transcript ends while retrying the gesture on truly empty home space, so final confirmation that settings opened is not captured in the transcript.

### C — Add Quick Actions to Home Footer

Status: substantially started across launcher UI, settings UI, and shared action model, but still uncommitted.

Work started:

- A new shared helper, `LauncherQuickActions.kt`, was added.
- The helper defines 3 footer slots and a stored spec model for actions.
- Supported quick-action specs were started for:
  - launcher settings
  - system settings
  - Wi-Fi settings
  - Bluetooth settings
  - display settings
  - app settings
  - open app
- Slot 1 defaults to `launcher_settings`, which preserves a visible settings entry point while the feature is unfinished.
- `activity_main.xml` replaces the home-only single bottom control with a new `footerActionsContainer` and increases bottom padding to fit footer labels/badges.
- `MainActivity.kt` adds `renderFooterActions()` and `createFooterActionView()` to build up to 3 footer buttons dynamically.
- The launcher keeps the existing bottom search button behavior in expanded view, while home view uses the footer action row.
- Footer notification behavior was started as a separate preference (`PREF_FOOTER_NOTIF_MODE`) with support for:
  - off
  - badge count
  - notification text
- Footer actions can launch apps or settings intents, and app-backed footer actions can show badge/text notifications using the existing notification data.
- `SettingsSystemFragment.kt` adds settings-side configuration for footer actions:
  - footer notification mode spinner
  - per-slot choose/clear rows
  - app picker for `open app`
- `fragment_settings_system.xml` and `strings.xml` add the required footer settings UI copy.

Transcript evidence:

- The prior run explicitly planned C as the replacement for the home footer while keeping expanded-view search behavior unchanged.
- The transcript records patches to `LauncherQuickActions.kt`, `activity_main.xml`, `fragment_settings_system.xml`, `strings.xml`, `MainActivity.kt`, and `SettingsSystemFragment.kt`.
- The transcript also records a successful build after one helper-file fix.
- Emulator validation reached C: the transcript says the new default footer slot was visible on the home screen, which confirms that the footer replacement was at least rendering at runtime.

## Short Handoff Summary

- Partially implemented features: **A, B, C**.
- Most complete-looking work: **C**, then **A**, then **B**.
- Best evidence of runtime behavior exists for **C**.
- **A** appears wired in code and UI but not runtime-verified in the transcript.
- **B** appears wired in code and partially tested, but the transcript cuts off before final success confirmation on empty home space.