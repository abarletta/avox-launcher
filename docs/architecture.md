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
- One primary screen
- One vertical list of launchable apps
- One favorites section at the top
- One search trigger using a system intent

The product is apps-first, not widget-first.
It reduces navigation friction and does not introduce new surfaces.

## Current Implementation Shape

The current app already follows the intended minimal shape:

- `MainActivity.kt` owns the launcher screen behavior
- `activity_main.xml` defines the single screen layout
- The app uses AppCompat and XML views
- The project has one Android app module only

Copilot should treat this as the default implementation path.

---

## v0 Scope

v0 is strict and minimal.

Allowed:

- Single screen
- Vertical app list
- Favorites at top
- Basic search via system intent

Not allowed:

- Widgets
- Folders
- Settings UI
- Custom animations
- Gesture systems beyond default launcher behavior

---

## Minimal Runtime Responsibilities

The launcher must:

- Read installed launchable apps from Android
- Display favorites first
- Display remaining apps below
- Launch apps
- Trigger system search

Nothing else.

---

## Implementation Constraints

- No repository layer
- No manager classes unless strictly required
- No service layer
- No state management framework
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
