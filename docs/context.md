# Project Context

## Use This File For

- Owner preferences
- Communication expectations
- Current repository state
- Questions about what project context Copilot should use

Read this after `docs/index.md` when the task is about loaded context, working style, or how to adapt answers to the project owner.

## Owner Profile

The project owner is highly technical.

Background:

- Quantitative finance

Preferences:

- Structured systems
- Minimalism
- Performance
- Short, clear solutions
- Low verbosity

## Risk

There is a strong tendency toward overengineering.
That tendency must be actively countered.

The default correction is:

- simplify
- narrow scope
- remove moving parts

## Current Codebase State

The repository is post initial scope with full customization features.

Current shape:

- One Android app module
- Kotlin + AppCompat + XML views
- Two activities: MainActivity (launcher) and SettingsActivity (fragment host)
- Five settings fragments: SettingsMenuFragment, SettingsAppearanceFragment, SettingsWallpaperFragment, SettingsAnimationsFragment, SettingsSystemFragment
- Custom views: AlphabetSidebar (letter animations)
- Bottom sheet: AppActionsSheet (long-press actions)
- Service: NotificationService (notification listener)
- Application: LauncherApp (custom font loading and language preference)
- Helpers: IconPackResolver (third-party icon pack support), LauncherQuickActions (footer action slots)
- 11 locale translations (da, de, es, fi, fr, it, nb, nl, pl, pt, sv)
- Minimal dependency set (core-ktx 1.13.1, AppCompat 1.7.0, Material 1.12.0)

Current implementation files:

- `app/src/main/java/com/avox/launcher/MainActivity.kt` — launcher screen, app list adapter, favorites grid (multi-column), widget lifecycle (host stays active, multi-widget slots with swipe), wallpaper effects, notification data, swipe gestures, sidebar setup, heading letter animation, list/widget transition animations, search, footer quick actions, WidgetFrame inner class
- `app/src/main/java/com/avox/launcher/SettingsActivity.kt` — fragment host activity with back navigation, theme application, and deep-link to specific settings screens
- `app/src/main/java/com/avox/launcher/SettingsMenuFragment.kt` — top-level menu with category cards, language selection, backup/restore
- `app/src/main/java/com/avox/launcher/SettingsAppearanceFragment.kt` — font, font size, spacing, alignment, margins, icon mode (regular/nerd/none), icon size, icon pack, nerd font picker, status bar toggle
- `app/src/main/java/com/avox/launcher/SettingsWallpaperFragment.kt` — theme (light/dark/system), wallpaper effects (darken/blur/color), darkness, blur radius, color tint, wallpaper select
- `app/src/main/java/com/avox/launcher/SettingsAnimationsFragment.kt` — sidebar animation style with per-style controls (wave/highlight/fade)
- `app/src/main/java/com/avox/launcher/SettingsSystemFragment.kt` — notification mode, swipe toggle, favorites management (picker with icons, reorder), widget management (add, remove, resize, full-width, controls), quick actions setup
- `app/src/main/java/com/avox/launcher/AlphabetSidebar.kt` — sidebar with wave/highlight/fade animation styles and configurable intensity/radius
- `app/src/main/java/com/avox/launcher/AppActionsSheet.kt` — app info, Play Store, uninstall, shortcuts
- `app/src/main/java/com/avox/launcher/NotificationService.kt` — notification listener with per-package dismiss
- `app/src/main/java/com/avox/launcher/LauncherApp.kt` — custom font loading and AppCompat locale application
- `app/src/main/java/com/avox/launcher/IconPackResolver.kt` — icon pack discovery and resolution with safe fallback
- `app/src/main/java/com/avox/launcher/LauncherQuickActions.kt` — quick actions singleton for 3 footer slots (launcher settings, system settings, wifi, bluetooth, display, apps, pick custom app)
- `app/src/main/res/layout/activity_main.xml` — launcher layout with darkOverlay, widgetContainer, favoritesGrid, appList, searchBar, alphabetSidebar, footer actions
- `app/src/main/res/layout/activity_settings.xml` — settings layout with wallpaper overlay and fragment container
- `app/src/main/res/layout/fragment_settings_menu.xml` — category cards for settings navigation
- `app/src/main/res/layout/fragment_settings_appearance.xml` — appearance controls layout
- `app/src/main/res/layout/fragment_settings_wallpaper.xml` — wallpaper controls layout
- `app/src/main/res/layout/fragment_settings_animations.xml` — animation controls layout
- `app/src/main/res/layout/fragment_settings_system.xml` — system/notifications/widgets/favorites layout
- `app/src/main/res/layout/item_app.xml` — app list row with dynamic icon sizing, name, notification text, badge
- `app/src/main/res/layout/item_favorite_grid.xml` — favorites grid cell with icon and badge
- `app/src/main/res/layout/item_widget_picker.xml` — widget picker row with app name, widget name, dimensions, icon
- `app/src/main/res/layout/item_widget_picker_header.xml` — widget picker category header
- `app/src/main/res/layout/sheet_app_actions.xml` — bottom sheet for app actions
- `app/src/main/res/layout/item_shortcut.xml` — shortcut row in actions sheet

Copilot should treat the existing implementation as the baseline and extend it conservatively.

## How AI Should Adapt

- Use Copilot models according to task depth, scope, and cost
- Be concise
- Be precise
- Avoid fluff
- Challenge unnecessary complexity
- Prefer short answers over long explanations
- Prefer concrete decisions over open-ended exploration
- Push toward simpler implementations when possible
- Keep changes close to the existing file structure unless there is a strong reason not to

## Communication Standard

Do not romanticize the project.
Do not pad the answer.
Do not propose extra systems unless they solve a real current problem.
