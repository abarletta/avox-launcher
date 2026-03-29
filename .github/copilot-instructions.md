# Avox Copilot Instructions

## Objective

- Build and maintain a deliberately minimal Android launcher
- Prefer the smallest direct change that solves the current problem
- Keep the codebase easy to inspect in one pass

## Required Artifact Read Path

For any repository-aware task, read `docs/index.md` first.

Use `docs/index.md` as the routing table for the rest of `docs/`.
Do not guess which artifact is relevant when the index already answers that question.

Minimum read set by task:

- Repository context, loaded context, or instruction questions:
  - `docs/index.md`
  - `docs/context.md`
  - `docs/architecture.md`
  - `docs/roadmap.md`
  - `docs/ai_roles.md`
- Implementation, refactor, bugfix, or review work:
  - `docs/index.md`
  - `docs/context.md`
  - `docs/architecture.md`
  - `docs/roadmap.md`
- Prompt, instruction, or Copilot-usage work:
  - `docs/index.md`
  - `docs/ai_roles.md`
  - `docs/copilot_v0_prompt.md`
  - `docs/copilot-models.json` if model capabilities or cost matter

If a question is about scope, architecture, owner preferences, model choice, or repository instructions, `docs/` is in scope and should be consulted.

For the first repository-aware turn, prefer reading the full core docs set above instead of only a subset.

## Current Repo Shape

- One Android app module only: `app`
- Kotlin + AppCompat + XML views
- Fragments used only in SettingsActivity; no Compose, no Navigation Component, no dependency injection, no extra modules
- 13 Kotlin source files, 13 layout XMLs, 11 locale translations

### Source files

- `app/src/main/java/com/avox/launcher/MainActivity.kt` — launcher screen, app list, favorites grid, widgets, wallpaper effects, notification data, swipe gestures, sidebar, search
- `app/src/main/java/com/avox/launcher/SettingsActivity.kt` — fragment host with back navigation and theme application
- `app/src/main/java/com/avox/launcher/SettingsMenuFragment.kt` — top-level settings menu, language selection, backup/restore
- `app/src/main/java/com/avox/launcher/SettingsAppearanceFragment.kt` — font, font size, spacing, alignment, icon mode/size/pack, nerd font
- `app/src/main/java/com/avox/launcher/SettingsWallpaperFragment.kt` — theme, wallpaper effects (darken/blur/color)
- `app/src/main/java/com/avox/launcher/SettingsAnimationsFragment.kt` — sidebar animation style and per-style controls
- `app/src/main/java/com/avox/launcher/SettingsSystemFragment.kt` — notification mode, favorites, widget management, quick actions
- `app/src/main/java/com/avox/launcher/AlphabetSidebar.kt` — custom View with wave/highlight/fade animations
- `app/src/main/java/com/avox/launcher/AppActionsSheet.kt` — bottom sheet for long-press actions
- `app/src/main/java/com/avox/launcher/NotificationService.kt` — notification listener with per-package dismiss
- `app/src/main/java/com/avox/launcher/LauncherApp.kt` — Application subclass for custom font loading and language preference
- `app/src/main/java/com/avox/launcher/IconPackResolver.kt` — icon pack discovery and resolution
- `app/src/main/java/com/avox/launcher/LauncherQuickActions.kt` — quick actions singleton for footer action slots

### Key layouts

- `app/src/main/res/layout/activity_main.xml` — launcher layout with widget container, favorites grid, app list, sidebar, search bar, footer actions
- `app/src/main/res/layout/activity_settings.xml` — settings layout with fragment container
- `app/src/main/res/layout/fragment_settings_menu.xml`, `fragment_settings_appearance.xml`, `fragment_settings_wallpaper.xml`, `fragment_settings_animations.xml`, `fragment_settings_system.xml` — settings screen layouts
- `app/src/main/res/layout/item_app.xml` — app list row
- `app/src/main/res/layout/item_favorite_grid.xml` — favorites grid cell
- `app/src/main/res/layout/item_widget_picker.xml`, `item_widget_picker_header.xml` — widget picker items
- `app/src/main/res/layout/sheet_app_actions.xml`, `item_shortcut.xml` — app actions bottom sheet

### Resources

- Strings and theme in `app/src/main/res/values/` (strings.xml, themes.xml, arrays.xml, launcher_identity.xml)
- 11 locale translations: da, de, es, fi, fr, it, nb, nl, pl, pt, sv

Copilot should preserve this shape unless a change is explicitly requested and justified.

---

## Decision Hierarchy

- Andrea is the final decision maker
- Copilot supports implementation, review, and reasoning
- No model defines product direction autonomously

If a request is ambiguous and the narrower option is not obvious, ask before broadening scope.

## Context Disclosure Rules

If the user asks what context is loaded, what files were read, or which instructions are active:

- List instruction and artifact files first
- Include `.github/copilot-instructions.md`
- Include `docs/index.md` and every `docs/` artifact actually read
- Include any loaded skill files if the environment exposes them
- Then list any repo code or config files read
- Explicitly state which relevant `docs/` files were not read yet, if any

Do not replace this list with a generic project summary.
Do not omit `docs/` artifacts that were actually read.

## Single-Token Test Handling

If the user explicitly says to answer only `YES` or `NO`:

- Reply with exactly one token: `YES` or `NO`
- Do not read more files in the same turn
- Do not explain, clarify, or take further action unless the user asks again

If the user later asks for clarification, explain the exact proposition that the `YES` or `NO` referred to.

---

## Model Selection

Use the guidance in `docs/copilot-models.json` pragmatically:

- Use fast models for small local edits, completions, and repetitive code: GPT-3.5 Turbo, GPT-5 mini, Claude Haiku 4.5, Gemini Flash 2.0, Grok Code Fast 1
- Use balanced models for most day-to-day coding and review work: GPT-4.1, GPT-4.5, Claude Sonnet 4.5
- Use large-context or deep-reasoning models for repo-wide changes, architecture review, and hard debugging: GPT-5, Gemini Pro 2.5
- Use GPT-4o when screenshots or visual debugging are part of the task
- Use Claude Opus 4.6 sparingly for the rare cases that justify elevated-cost audit-style reasoning

Default bias:
- Prefer the cheapest model that is strong enough for the task
- Escalate model depth only when the task actually needs it

---

## Default Working Style

- Solve the smallest real problem
- Keep flows simple and readable
- Prefer hardcoded over configurable in v0
- Delete complexity aggressively
- Prefer platform APIs over custom infrastructure
- Prefer direct code over future-proofing
- Stop when the simplest working version exists

---

## Hard Constraints

- No feature unless explicitly requested
- No abstraction without 2 real use cases
- No configuration system
- No extension points "for later"
- No new dependencies without explicit justification
- No architecture redesign during implementation

If unsure, do nothing and ask.

---

## Implementation Rules

- Start with the existing files before creating new ones
- Prefer changes in existing source files over new files
- Keep app loading, sorting, favorites ordering, and search behavior close to the launcher screen unless complexity forces separation
- Use direct data flow and the smallest necessary state
- Do not move to new layers just to make the code look more "architected"

Stop condition:
- If the feature works and the code is still simple, stop

---

## Architecture Guardrails

- One home screen
- Vertical app list
- Favorites at the top
- Widget host on home screen
- Settings in SettingsActivity
- Default Android behavior wherever possible
- No folders
- No multiple home screens
- No custom navigation framework

---

## Anti-Patterns

- Repository layer without real need
- ViewModel or state manager without real complexity
- Manager or service classes added for organization alone
- Plugin or modular architecture
- Configuration systems
- Feature flags
- Generic utility layers
- Overly flexible data models

---

## Scope Filter

Before adding anything, all must be true:

1. It is required for the current scope
2. No simpler alternative exists
3. Android does not already solve it well enough
4. It has at least 2 real use cases if it introduces abstraction

If any answer is no, do not add it.

---

## Project Context

- The project owner is highly technical
- Background: quantitative finance
- Preferences: structured systems, minimalism, performance, short clear solutions, low verbosity
- Overengineering is the default failure mode and must be actively countered
- Default correction: simplify, narrow scope, remove moving parts
- Be concise, precise, and concrete
- Do not romanticize the project
- Do not pad the answer
- Do not propose extra systems unless they solve a real current problem

---

## Roadmap Context

- The launcher is post-v0 with full customization features
- Treat the current implementation as the baseline
- Future candidates are not commitments
- If an item adds product surface area, it should probably be rejected or deferred
