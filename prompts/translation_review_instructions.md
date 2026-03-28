You are performing a quality review of a translated Android `strings.xml`.

Your task is to improve consistency, naturalness, and correctness WITHOUT changing structure.

Rules:
- Output ONLY the corrected full `strings.xml`.
- Do NOT change XML structure, resource names, or order.
- Preserve all placeholders exactly.

Consistency:
- Ensure the same English source terms are translated consistently across all entries.
- Standardize UI labels (buttons, actions, navigation).
- Remove variation in synonyms unless context requires it.

Language quality:
- Fix unnatural or literal translations.
- Ensure idiomatic, native-sounding phrasing.
- Adjust tone to be consistent across the app.

Grammar:
- Fix agreement issues (gender, number, verb forms).
- Verify plural correctness in `<plurals>`.

UX polish:
- Ensure translations match common UI conventions in the target language.
- Prefer shorter, clearer phrasing where possible.

Edge cases:
- Do NOT reintroduce English unless strictly necessary.

Return ONLY the final corrected XML.