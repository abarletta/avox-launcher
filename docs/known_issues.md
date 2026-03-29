# Known Issues

Issues and inconsistencies identified during a deep project scan on 2026-03-29.

## Build Configuration

- **`isMinifyEnabled = false` in release build** — `app/build.gradle.kts` has minification disabled for the release build type. This means the release APK ships without R8 shrinking, obfuscation, or dead-code removal. Enabling it would reduce APK size and improve runtime performance, but requires testing for reflection or resource-stripping issues.

- **`versionCode = 1` / `versionName = "0.1.0"`** — The project is described as "post initial scope" with full customization features, but the version metadata still reflects a pre-release state. If the app is distributed (Play Store or otherwise), versionCode should be incremented with releases.

- **`compileSdk = 34` / `targetSdk = 34`** — API 34 (Android 14) is current but may soon fall behind Play Store target SDK requirements. Worth monitoring.

## Manifest

- **`android:supportsRtl="false"`** — The app supports 11 locale translations. While none are currently RTL languages, this flag would block future RTL support (Arabic, Hebrew, etc.) without a manifest change. If RTL languages are never planned, this is intentional.

- **No `BIND_APPWIDGET` permission declared** — Widget hosting uses `AppWidgetHost` which works via the system bind flow, so this is not strictly required. However, the absence means the app cannot silently bind widgets and must always prompt the user via the system dialog.

## Missing Test Infrastructure

- No unit tests (`app/src/test/` does not exist)
- No instrumented tests (`app/src/androidTest/` does not exist)
- No test dependencies in `app/build.gradle.kts`

## Dependencies

- **Only 3 runtime dependencies** — `core-ktx:1.13.1`, `appcompat:1.7.0`, `material:1.12.0`. This is minimal and correct per project philosophy. No unnecessary transitive dependency exposure detected.

## Resource Completeness

- **10 drawable XML files, 0 PNGs** — All icons are vector drawables. Clean and resolution-independent.
- **No bundled font files** — Custom fonts are loaded from user-provided TTF files on the file system, not from `res/font/` or `assets/`. This is by design (user picks their font).

## Artifact Inconsistencies (Fixed)

The following were identified as stale and corrected in this update:

- `docs/context.md` listed `SettingsActivity.kt` as owning "all settings controls" — it is actually a fragment host only. Fixed.
- `docs/context.md` was missing 8 source files (all settings fragments, LauncherQuickActions) and 9 layout files. Fixed.
- `docs/architecture.md` Non-Goals listed "Widgets", "Settings UI", "Custom animation systems", and "Theming systems" as out of scope, contradicting their actual implementation. Fixed.
- `docs/architecture.md` was missing `LauncherQuickActions.kt` and 3 layout files. Fixed.
- `docs/roadmap.md` baseline was missing 6 implemented features (multi-column favorites, quick actions, backup/restore, multi-widget, multilingual, home long-press). Fixed.
- `README.md` was missing features and build tool versions. Fixed.
- `docs/index.md` was missing artifact map entries for `feature_wishlist.md`, `missing_features.md`, `localization_add_language.md`, and `known_issues.md`. Fixed.
- `docs/visual_code_map.md` was missing sections for favorites grid, quick actions footer, and widget picker. Fixed.
- `.github/copilot-instructions.md` stated "No fragments" while the project uses 5 settings fragments; source/layout file lists were incomplete. Fixed.
