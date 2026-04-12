# Architecture

## Use This File For

- System shape
- Implementation boundaries
- File and layering constraints
- Questions about what code structure Copilot should preserve

Read this after `docs/index.md` before making code changes or answering architecture questions.

## High-Level System

Avox is a single-purpose Android home launcher.

The system must remain minimal:

- One launcher entry point
- One primary screen with vertical app list
- Favorites at top, remaining apps below
- Widget host on home screen
- Full settings activity for customization
- Notification listener for badges and text
- Alphabet sidebar with multiple animation styles

The product is apps-first, not widget-first.

## Current Implementation Shape

### Source files

- `MainActivity.kt` — launcher screen, app list adapter (with icon mode: regular/nerd/none), widget lifecycle (host stays active, deferred finalization via onResume), wallpaper effects (darken/color), notification data, swipe gestures, sidebar setup, heading letter animation, list/widget transition animations
- `SettingsActivity.kt` — fragment host activity with back navigation and theme application
- `SettingsMenuFragment.kt` — top-level menu with category cards (Appearance, Wallpaper, Animations, Notifications, Widgets)
- `SettingsAppearanceFragment.kt` — font, font size, spacing, alignment, margins, block count, icon mode (regular/nerd/none), icon size, icon pack, nerd font picker
- `SettingsWallpaperFragment.kt` — theme, wallpaper effects (darken/color), darkness, color tint, wallpaper select
- `SettingsAnimationsFragment.kt` — sidebar animation style with per-style controls (wave/highlight/fade)
- `SettingsSystemFragment.kt` — notification mode, swipe toggle, favorites management (picker with icons, reorder), widget management (add, remove, resize, full-width, controls), quick actions setup
- `AlphabetSidebar.kt` — custom View with wave/highlight/fade animation styles and configurable intensity/radius
- `AppActionsSheet.kt` — bottom sheet for long-press actions (app info, Play Store, uninstall, shortcuts)
- `NotificationService.kt` — NotificationListenerService with per-package dismiss
- `LauncherApp.kt` — Application subclass for custom font loading and language preference application
- `IconPackResolver.kt` — discovers installed icon packs, resolves per-app icons with safe fallback
- `LauncherQuickActions.kt` — quick actions singleton managing 3 footer slots with system/app intents

### Layout files

- `activity_main.xml` — FrameLayout with darkOverlay, widgetContainer, favoritesGrid, appList, searchBar, alphabetSidebar, footer actions
- `activity_settings.xml` — FrameLayout with wallpaper overlay and fragment container
- `fragment_settings_menu.xml` — category cards for settings navigation
- `fragment_settings_appearance.xml` — appearance controls layout
- `fragment_settings_wallpaper.xml` — wallpaper controls layout
- `fragment_settings_animations.xml` — animation controls layout
- `fragment_settings_system.xml` — system/notifications/widgets/favorites layout
- `item_app.xml` — app row with dynamic icon sizing, name, notification text, badge
- `item_favorite_grid.xml` — favorites grid cell with icon and badge
- `item_widget_picker.xml` — widget picker row with app name, widget name, dimensions, icon
- `item_widget_picker_header.xml` — widget picker category header
- `sheet_app_actions.xml` — bottom sheet for app actions
- `item_shortcut.xml` — shortcut row in actions sheet

### Data flow

- All settings stored in SharedPreferences
- Widget state managed by AppWidgetHost/AppWidgetManager
- Notification data flows from NotificationService → NotificationHolder → MainActivity
- Icon pack resolution is lazy and falls back to system icons

---

## Implementation Constraints

- One app module only
- Kotlin + AppCompat + XML views
- Fragments used only in SettingsActivity, no Compose, no Navigation Component
- No dependency injection
- No repository layer
- No manager classes unless strictly required
- No service layer beyond NotificationListenerService
- No state management framework
- minSdk 26, targetSdk 34

---

## Feature Boundaries

Implemented:

- Vertical app list with favorites and section headers in expanded view
- Multi-column favorites grid (adaptive column count by screen size/orientation)
- Widget host with add/bind/configure/restore/resize
- Multi-widget slots with horizontal swipe navigation between widgets in the same slot
- Widget management in settings (list, remove, height adjustment, full-width toggle, controls)
- Settings activity with wallpaper-matched background and fragment-based navigation
- Wallpaper effects: darken, color tint
- Notification badges (count) and inline text
- Swipe-to-dismiss notifications
- Three sidebar animation styles with per-style controls
- Content alignment (left/center) for entire row (icon+label), margins (h: 0–120dp), spacing
- Sidebar position synced with app list content area
- Font selection (system + custom TTF)
- Icon size control
- Third-party icon pack support
- Nerd font icon prefixes
- App long-press actions (info, store, uninstall, shortcuts)
- Local search filtering
- Quick actions footer (up to 3 customizable action slots)
- Settings backup and restore (JSON export/import)
- Language selection with 11 locale translations
- Home screen long-press to open settings

Not implemented:

- Folders
- Multiple home screens
- Gesture navigation beyond swipe-to-dismiss
- Built-in widgets
- Feed pages
- Recommendation systems
- No background processing unless required for correctness
- No extra Gradle modules
- No migration to Compose unless explicitly requested

The system should be implementable with a small number of classes.

Prefer:
- direct data flow
- simple lists
- minimal transformation logic
- edits to existing files before new files

---

## Data Model

- Use the simplest representation of an app (label, package name, intent)
- Do not introduce complex domain models
- Do not wrap platform objects unless necessary

---

## State Rules

- Store only what is required to render the screen
- Avoid caching unless performance issues are measured
- Avoid derived state if it can be recomputed cheaply

Favorites persistence (if implemented):
- use simple local storage
- no abstraction layer
- no synchronization logic

---

## Non-Goals

Explicitly out of scope:

- Folders
- Multiple home screens
- Gesture systems beyond swipe-to-dismiss and home long-press
- Feed pages
- Smart recommendations
- Plugin or extension architecture
- General-purpose customization beyond current feature set
- Background processing unless required for correctness
- Extra Gradle modules
- Migration to Compose unless explicitly requested

---

## Design Principle

- If a solution introduces a new layer, it must be justified
- If two solutions work, choose the simpler one
- If a feature is optional, do not implement it
- Copilot output should preserve this minimal structure
