# Adding a New Language (for contributors)

This document explains the minimal, correct steps to add a new language to Avox Launcher so that the UI can appear in that language and contributors' translations integrate cleanly.

Keep changes small and focused: do not change resource keys, and keep placeholders intact when translating formatted strings.

## Quick summary
- Create a `values-<lang>/strings.xml` (e.g. `values-es` for Spanish).
- Translate every string key from the base `values/strings.xml` without renaming keys.
- Add the language to the settings UI (see Options below).
- Build and test on-device (switch language in Settings → Language).

## Requirements
- Familiarity with Android resources and building the app with Gradle.
- Keep all translation text in resource files only; do not hardcode strings in Kotlin or layout XML.

## Step-by-step

1) Create the locale resource folder

   - Under `app/src/main/res/` create a new folder named `values-<lang>` (example: `values-es` for Spanish, `values-fr` for French, `values-pt-rBR` for Portuguese Brazil). Use the standard Android qualifiers.
   - Copy `app/src/main/res/values/strings.xml` into the new folder as `strings.xml`.

2) Translate the strings

   - Translate every value in the copied `strings.xml`. Do NOT change the XML keys.
   - Preserve formatting placeholders exactly: if the original contains `%1$s`, `%d`, or `%s`, keep them in the translation and in the same order unless you also adjust positional indices intentionally.
   - Preserve HTML-like formatting tags and escaped entities (e.g. `&amp;`).
   - For `plurals` keep the same quantity keys (`one`, `other`, etc.) and translate the values.

3) Localize arrays used by the UI

   - The language picker UI may use arrays or hardcoded lists in code. Preferred approach:
     - Add a resource file `app/src/main/res/values/arrays.xml` (if missing) with two parallel string-arrays: `language_codes` and `language_labels`.
     - Example (base `values/arrays.xml`):

```xml
<resources>
  <string-array name="language_codes">
    <item></item> <!-- system default (empty) -->
    <item>en</item>
    <item>es</item>
  </string-array>

  <string-array name="language_labels">
    <item>System default</item>
    <item>English</item>
    <item>Español</item>
  </string-array>
</resources>
```

   - In each `values-<lang>/arrays.xml` translate only the `language_labels` entries; `language_codes` must remain the same.
   - If the launcher currently uses a hardcoded spinner in `SettingsMenuFragment.kt`, update it to consume `@array/language_codes` and `@array/language_labels` (preferred) or add the new language option there.

4) Wire the locale code

   - The app stores the selected language code in preferences (key: `PREF_LANGUAGE` / `app_language`). When the user selects a language, the code (e.g. `es`) must be saved.
   - If you add a new language option in code, ensure its stored value matches the corresponding locale code in `language_codes`.

5) Handle special cases

   - Right-to-left languages: verify `android:supportsRtl` in `AndroidManifest.xml` and test layout direction.
   - Date/time/number formats: use Android locale APIs (do not hardcode formats).

6) Test locally

   - Build debug and install on a device/emulator:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

   - Open Settings → Language in the launcher, choose the new language, and confirm all setting screens and the home UI show translated text.
   - Test backup & restore flows to ensure `app_language` is included and reapplied after restore.

7) Submit a PR

   - Include:
     - The new `values-<lang>/strings.xml` (and any `arrays.xml` overrides).
     - A brief note in the PR description with the locale code and language name.
     - Screenshots of the settings screen and the home screen in the new language.
     - Any notes about placeholder reordering or plural edge-cases.

## Developer notes and best practices

- Never rename existing resource keys; adding new keys is allowed if the translation requires new phrases.
- Keep translations concise; long strings can break UI layouts. If a string is long, propose a shorter variant or open an issue to adjust layouts.
- Use formal punctuation consistent with the language norms.
- For collaborative translations, prefer one PR per locale.

## Optional: Automating language discovery (advanced)

- Instead of hardcoding language options in Kotlin, the launcher can read `R.array.language_codes` and `R.array.language_labels` at runtime. That approach lets translators supply localized labels without code changes.
- If you implement that change, include a migration note in the PR so other contributors can add languages by updating resources only.

## Credits

Thank you for contributing translations. Add your name and language to the PR description to receive credit.

---