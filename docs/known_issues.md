# Known Issues

Status updated on 2026-03-31 after the first-release hardening pass.

## Open Issues

- None currently identified as release blockers.

## Resolved In This Pass

- Release builds now enable R8 minification and resource shrinking.
- Version metadata now reflects the first public release (`versionCode = 2`, `versionName = "1.0.0"`).
- `compileSdk` and `targetSdk` now target API 35, which meets the current Google Play requirement for new apps and updates.
- `android:supportsRtl` is enabled so future RTL locale support is not blocked at the manifest level.
- Test infrastructure is now present in both `app/src/test/` and `app/src/androidTest/`, with matching Gradle dependencies and a working instrumentation runner.

## Platform Constraint

- Widget binding intentionally uses the system bind flow. `android.permission.BIND_APPWIDGET` is not for use by third-party applications, so silent widget binding is not a supported path for this launcher.