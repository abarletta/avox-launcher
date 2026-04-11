# Avox Launcher

Avox is a minimal Android launcher built around a single screen, a vertical app list, fast access, and deliberate limits. Inspired by Niagara Launcher.

## Download

**[Download Avox Launcher APK](https://github.com/abarletta/avox-launcher/releases/latest)**

Requires Android 8.0 (API 26) or higher.

## Installation

1. Download the APK from the link above.
2. On your Android device, enable **Install from unknown sources** for your browser or file manager if prompted. 
3. Open the APK and tap **Install**.
4. After installation, press the Home button and select **Avox** as your launcher.
5. Grant **Notification Access** when prompted (required for notification badges).

> **Note**: On Samsung devices, you may need to disable the "Auto Blocker" feature from Settings > Security and privacy. This must be done in addition to enabling installing from unknown sources. 

## Features

### Home Screen
- Vertical app list with alphabetical sidebar navigation
- Favorites grid at top (multi-column, adaptive layout)
- Widget host with multi-widget slot support (swipe between stacked widgets)
- Local search bar for instant app filtering
- Quick actions footer (up to 3 customizable action slots)
- Long-press home screen to access settings

### Notifications
- Notification badges (count mode)
- Inline notification text preview (text mode)
- Swipe-to-dismiss notifications per app
- Configurable notification mode (badges, text, or off)

### App Actions
- Long-press any app for quick actions: App Info, Play Store, Uninstall, Shortcuts

### Customization
- **Themes:** light, dark, or follow system
- **Fonts:** system fonts, custom TTF files, configurable size (12–50sp)
- **Icons:** regular app icons, Nerd Font icon prefixes, or no icons; adjustable size (16–48dp)
- **Icon packs:** third-party icon pack support (ADW, Nova, Tesla, GO Launcher formats)
- **Layout:** content alignment (left/center), horizontal and vertical margins, item spacing
- **Wallpaper effects:** darken, blur (sampled wallpaper blur overlay), color tint (6 presets)
- **Sidebar animations:** wave/zoom, highlight, or fade — each with per-style controls
- **Status bar:** option to hide

### Widget Management
- Add unlimited widget slots with height and full-width controls
- Stack multiple widgets per slot with swipe navigation
- Widget picker with app grouping
- Widget configuration, removal, and reordering from Settings
- Widget state persisted across restarts and included in settings backup

### Settings & Data
- Fragment-based settings with wallpaper-matched background
- Settings backup and restore (JSON export/import)
- Language selection with 12 languages: English, Danish, German, Spanish, Finnish, French, Italian, Dutch, Norwegian Bokmål, Polish, Portuguese, Swedish

## Philosophy

- Minimal UI, apps-first navigation
- Fast access with low cognitive load
- No folders, no multiple home screens, no bloat

## Technical Details

- Kotlin + AppCompat + XML views
- Single app module, fragment-based settings
- minSdk 26, targetSdk 35
- AGP 9.1.0, Kotlin 2.2.10, Gradle 9.3.1
- Dependencies: core-ktx 1.13.1, AppCompat 1.7.0, Material 1.12.0
- R8 minification and resource shrinking enabled for release builds

## Building from Source

```bash
git clone https://github.com/abarletta/avox-launcher.git
cd avox-launcher
./gradlew assembleRelease
```

The release APK will be at `app/build/outputs/apk/release/app-release.apk`.

## Project Documentation

- [docs/index.md](docs/index.md) — documentation routing table
- [docs/architecture.md](docs/architecture.md) — system shape and constraints
- [docs/roadmap.md](docs/roadmap.md) — scope and future candidates
- [docs/context.md](docs/context.md) — project context and preferences
- [docs/visual_code_map.md](docs/visual_code_map.md) — UI ownership map
- [docs/feature_wishlist.md](docs/feature_wishlist.md) — feature ideas and status
- [docs/localization_add_language.md](docs/localization_add_language.md) — how to add translations
- [docs/known_issues.md](docs/known_issues.md) — known issues and technical debt

## License

This project is for personal use.
