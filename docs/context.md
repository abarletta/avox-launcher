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

The repository is post-v0 with full customization features.

Current shape:

- One Android app module
- Kotlin + AppCompat + XML views
- Two activities: MainActivity (launcher) and SettingsActivity
- Custom views: AlphabetSidebar (letter animations)
- Bottom sheet: AppActionsSheet (long-press actions)
- Service: NotificationService (notification listener)
- Application: LauncherApp (custom font loading)
- Helper: IconPackResolver (third-party icon pack support)
- Minimal dependency set (AppCompat 1.7.0, Material 1.12.0)

Current implementation files:

- `app/src/main/java/com/alauncher/MainActivity.kt` — launcher screen, app list, widgets, wallpaper effects, notification data, swipe gestures
- `app/src/main/java/com/alauncher/SettingsActivity.kt` — all settings controls and preference persistence
- `app/src/main/java/com/alauncher/AlphabetSidebar.kt` — sidebar with wave/highlight/fade animations
- `app/src/main/java/com/alauncher/AppActionsSheet.kt` — app info, Play Store, uninstall, shortcuts
- `app/src/main/java/com/alauncher/NotificationService.kt` — notification listener with swipe-to-dismiss
- `app/src/main/java/com/alauncher/LauncherApp.kt` — custom font loading
- `app/src/main/java/com/alauncher/IconPackResolver.kt` — icon pack discovery and resolution
- `app/src/main/res/layout/activity_main.xml` — launcher layout
- `app/src/main/res/layout/activity_settings.xml` — settings layout
- `app/src/main/res/layout/item_app.xml` — app list row

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
