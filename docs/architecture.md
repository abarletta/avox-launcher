# Architecture

## Use This File For

- System shape
- Implementation boundaries
- File and layering constraints
- Questions about what code structure Copilot should preserve

Read this after `docs/index.md` before making code changes or answering architecture questions.

## High-Level System

A-Launcher is a single-purpose Android home launcher.

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

- `MainActivity.kt` — launcher screen, app list adapter, widget lifecycle, wallpaper effects (darken/blur/color), notification data, swipe gestures, sidebar setup
- `SettingsActivity.kt` — all preference controls: theme, wallpaper effects, font, font size, icon size, icon pack, nerd fonts, spacing, notifications, swipe toggle, animations, alignment, margins, block count, favorites, widgets
- `AlphabetSidebar.kt` — custom View with wave/highlight/fade animation styles and configurable intensity/radius
- `AppActionsSheet.kt` — bottom sheet for long-press actions (app info, Play Store, uninstall, shortcuts)
- `NotificationService.kt` — NotificationListenerService with per-package dismiss
- `LauncherApp.kt` — Application subclass for custom font loading
- `IconPackResolver.kt` — discovers installed icon packs, resolves per-app icons with safe fallback

### Layout files

- `activity_main.xml` — FrameLayout with darkOverlay, widgetContainer, appList, searchBar, alphabetSidebar, bottomButton
- `activity_settings.xml` — ScrollView with all settings controls, wrapped in FrameLayout with wallpaper overlay
- `item_app.xml` — app row with dynamic icon sizing, name, notification text, badge
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
- No fragments, no Compose, no Navigation Component
- No dependency injection
- No repository layer
- No manager classes unless strictly required
- No service layer beyond NotificationListenerService
- No state management framework
- minSdk 26, targetSdk 34

---

## Feature Boundaries

Implemented:

- Vertical app list with favorites
- Widget host with add/bind/configure/restore/resize
- Settings activity with wallpaper-matched background
- Wallpaper effects: darken, blur (downscale-upscale), color tint
- Notification badges (count) and inline text
- Swipe-to-dismiss notifications
- Three sidebar animation styles with per-style controls
- Content alignment (left/center), margins, spacing
- Font selection (system + custom TTF)
- Icon size control
- Third-party icon pack support
- Nerd font icon prefixes
- App long-press actions (info, store, uninstall, shortcuts)
- Local search filtering

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

- Widgets
- Folders
- Settings UI
- Custom animation systems
- Gesture systems beyond default behavior
- Theming systems
- Notification centers
- Feed pages
- Smart recommendations
- Plugin or extension architecture
- General-purpose customization

---

## Design Principle

- If a solution introduces a new layer, it must be justified
- If two solutions work, choose the simpler one
- If a feature is optional, do not implement it
- Copilot output should preserve this minimal structure
