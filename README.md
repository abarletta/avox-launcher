# Avox

Avox is a personal Android launcher built around a Niagara-like idea:
one screen, a vertical app list, fast access, and deliberate limits.

## Features

- Vertical app list with favorites at top (multi-column grid)
- Widget host on home screen with multi-widget slot support
- Wallpaper effects: darken, blur, color tint
- Notification badges and inline text
- Swipe gestures for notification actions
- Three sidebar animation styles (wave, highlight, fade)
- Full customization: fonts, icon size, icon packs, Nerd font prefixes, alignment, margins, spacing
- Local app search
- App long-press actions (info, store, uninstall, shortcuts)
- Quick actions footer (up to 3 customizable slots)
- Settings backup and restore
- Multilingual support (12 languages: en, da, de, es, fi, fr, it, nb, nl, pl, pt, sv)

## Philosophy

- Minimal UI
- Vertical, apps-first navigation
- Fast access with low cognitive load
- No bloat

## Technical

- Kotlin + AppCompat + XML views
- Single app module, fragment-based settings
- minSdk 26, targetSdk 34
- AGP 9.1.0, Kotlin 2.2.10, Gradle 9.3.1
- Dependencies: core-ktx 1.13.1, AppCompat 1.7.0, Material 1.12.0

## Project Docs

- `docs/index.md`
- `docs/architecture.md`
- `docs/ai_roles.md`
- `docs/roadmap.md`
- `docs/context.md`
- `docs/visual_code_map.md`
- `docs/feature_wishlist.md`
- `docs/localization_add_language.md`
- `.github/copilot-instructions.md`
