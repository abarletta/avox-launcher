# Avox Launcher v1.0.0

**Release Date:** 2026-03-31

**Download:** [app-release.apk](https://github.com/abarletta/avox-launcher/releases/download/v1.0.0/app-release.apk)

---

## About

Avox Launcher is a minimal Android home launcher focused on fast app access through a clean vertical list, an alphabetical sidebar, and deliberate limits. No folders, no multiple screens, no bloat.

**Requires Android 8.0 (API 26) or higher.**

---

## What's New in v1.0.0

This is the first public release of Avox Launcher. All features below are new.

### Home Screen

- **Vertical app list** with alphabetical section headers
- **Favorites grid** at top with adaptive multi-column layout (adjusts to screen size and orientation)
- **Alphabet sidebar** for instant letter-based navigation with touch tracking
- **Local search bar** for filtering installed apps in real time
- **Quick actions footer** — up to 3 customizable action slots (launcher settings, system settings, Wi-Fi, Bluetooth, display, apps, or any installed app)
- **Long-press home screen** to open launcher settings

### Widgets

- Full **third-party widget host** (add, bind, configure, restore, resize)
- **Multi-widget slots** — stack multiple widgets per slot and swipe between them
- **Widget picker** with app grouping, icons, and widget dimensions
- Per-widget **height control** and **full-width toggle**
- Widget controls/settings access per widget
- **Slot indicator** showing active widget position
- Widget state persisted across restarts

### Notifications

- **Badge count mode** — numeric badge on app icons
- **Notification text mode** — inline text preview from apps
- **Off mode** — hide badges entirely
- **Swipe-to-dismiss** — swipe on an app to dismiss its notifications
- Requires Notification Access permission (prompted on first use)

### App Long-Press Actions

- **App Info** — open system app details
- **Play Store** — open app's Play Store page
- **Uninstall** — remove the app
- **Shortcuts** — access app shortcuts (Android 7.1+)

### Customization

- **Themes:** light, dark, or follow system
- **Fonts:** Default, Light, Thin, Condensed, Serif, Monospace, or custom TTF from device storage
- **Font size:** 12–50sp (default 16sp)
- **Sidebar font size:** 8–32sp (default 12sp)
- **Icon modes:** regular app icons, Nerd Font icon prefixes, or no icons
- **Icon size:** 16–48dp with dynamic glyph scaling
- **Icon packs:** third-party icon pack support (ADW, Nova, Tesla Coils, GO Launcher formats) with safe fallback
- **Content alignment:** left or center (applies to icon + label row)
- **Margins:** horizontal 0–50dp, vertical 0–50dp
- **Item spacing:** Compact (4dp), Normal (8dp), Spacious (14dp), Large (20dp)
- **Status bar:** option to hide

### Wallpaper Effects

- **Darken** — adjustable darkness overlay (0–100%)
- **Blur** — system blur-behind on API 31+, downscale-upscale fallback on older devices
- **Color tint** — 6 color presets (Indigo, Green, Red, Purple, Teal, Orange) with darkness control
- **Wallpaper selection** from device storage

### Sidebar Animations

Three animation styles with per-style controls:

- **Wave/Zoom** — text shift distance, zoom scale, wave radius
- **Highlight** — intensity-based scale and opacity
- **Fade** — distance-based opacity falloff

### Settings

- **Fragment-based navigation** across 6 categories: Appearance, Wallpaper, Animations, Notifications, Home, Widgets
- **Wallpaper-matched settings background** for visual consistency
- **Backup & restore** — export all settings to shareable JSON file, import with version validation
- Widget state included in backup/restore

### Localization

12 languages supported:

| Language | Code |
|---|---|
| English | en |
| Danish | da |
| German | de |
| Spanish | es |
| Finnish | fi |
| French | fr |
| Italian | it |
| Dutch | nl |
| Norwegian Bokmål | nb |
| Polish | pl |
| Portuguese | pt |
| Swedish | sv |

Language can be changed from Settings → Language, or defaults to system language.

---

## Known Limitations

- **Widget binding** uses the standard system bind flow. Silent widget binding is not supported for third-party launchers (platform constraint).
- **Horizontal/landscape layout** works but is not yet optimized — planned for a future release.
- **Built-in widgets** (e.g., clock, media player) are not included. Only third-party widgets are supported.
- **App open/close animations** use system defaults. Custom transition animations are deferred.

---

## Technical Details

- **Language:** Kotlin
- **UI:** AppCompat + XML views (no Compose)
- **Architecture:** single app module, 13 Kotlin source files, 13 layout XMLs
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **Build:** AGP 9.1.0, Kotlin 2.2.10, Gradle 9.3.1
- **Dependencies:** core-ktx 1.13.1, AppCompat 1.7.0, Material 1.12.0
- **Release:** R8 minification and resource shrinking enabled

---

## Installation

1. Download the APK from the link above.
2. Enable **Install from unknown sources** on your device if prompted.
3. Install and press Home — select **Avox** as your default launcher.
4. Grant **Notification Access** when prompted for badge support.
