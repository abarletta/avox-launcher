# Copilot v0 Prompt

Use this prompt when asking Copilot to implement or review v0 work in this repository.

Read `docs/index.md` first if you are locating artifacts rather than directly using this prompt.

Before changing code, read these files and treat them as the source of truth:

- `README.md`
- `docs/index.md`
- `docs/context.md`
- `docs/architecture.md`
- `docs/roadmap.md`
- `docs/ai_roles.md`
- `.github/copilot-instructions.md`

Repository status:

- The Android project already exists
- The codebase is not docs-only
- The app is intentionally small and should stay that way

Project definition:

- A-Launcher is a personal Android launcher
- It is intentionally narrow, minimal, and not a general-purpose launcher
- The philosophy is one screen, vertical app list, fast access, low cognitive load, minimal UI, and no bloat

Scope for v0:

- One launcher entry point
- One primary screen only
- One vertical list of installed launchable apps
- One favorites section at the top
- One search trigger using a system intent
- Tapping an app launches it

Out of scope and forbidden for v0:

- Widgets
- Folders
- Settings UI
- Custom animations
- Gesture systems beyond default launcher behavior
- Theming systems
- Notification centers
- Feed pages
- Smart recommendations
- Plugin or extension architecture
- Configuration systems

Implementation rules:

- Build the smallest working native Android change that satisfies the request
- Use Kotlin
- Keep a single Android app module only
- Keep the package structure flat and obvious
- Prefer a single activity
- Do not use fragments
- Do not use the Navigation Component
- Prefer the existing AppCompat + XML approach unless a different UI stack is explicitly requested
- Keep code explicit and direct
- No repository layer
- No manager classes unless strictly required
- No service layer
- No state-management framework
- No dependency injection framework
- No background processing unless required for correctness
- No abstraction without 2 real use cases
- Prefer duplication over premature abstraction
- Prefer platform APIs over custom infrastructure
- Stop when the simplest working version exists

Preferred change path:

- Start with `app/src/main/java/com/alauncher/MainActivity.kt`
- Then `app/src/main/res/layout/activity_main.xml`
- Then `app/src/main/res/values/strings.xml`
- Then `app/src/main/res/values/themes.xml`
- Create new files only when the existing shape becomes meaningfully worse

Data and state rules:

- Read installed launchable apps directly from Android
- Use the simplest app representation possible, for example: label, package name, launch intent
- Avoid wrapping platform types unless necessary
- Store only state required to render the screen
- Avoid caching unless a real measured problem appears
- Do not create a custom app state container unless it is strictly required to make the screen work

Favorites rule for v0:

- Do not build favorites management UI
- Do not add settings to manage favorites
- Keep favorites simple and code-driven for the first version
- A small hardcoded list of favorite package names is acceptable for v0
- Show matching installed favorites at the top, then the remaining apps below

Search rule for v0:

- Do not build in-app search
- Provide a simple visible search trigger that launches the most appropriate standard Android system search intent
- Keep this implementation minimal and platform-first

UI rules:

- One screen only
- Sparse layout
- Vertical list
- Favorites first
- Remaining apps below
- No extra surfaces
- No decorative work unless required for clarity
- Do not introduce extra UI architecture around the single screen

Anti-patterns to avoid:

- `AppModel`
- `AppDataSource`
- `LauncherState`
- `UiState`
- repository classes
- manager classes
- service classes
- use-case or interactor classes
- event buses
- feature flags
- configuration systems
- plugin architecture
- generic utility layers added only for future flexibility

Preferred code shape:

- Keep app loading, sorting, and launch behavior close to the launcher screen
- A small adapter helper is fine if the current UI stack requires it
- Favor direct platform calls over wrapper classes

Review mode:

- Prioritize bugs, regressions, scope violations, and unnecessary architecture
- Call out changes that expand product surface without explicit approval
- Treat missing tests or unverifiable behavior as a risk, not a reason to invent infrastructure

Decision policy:

- If a choice expands scope, reject it
- If two options work, choose the simpler one
- If something is optional, do not implement it
- If something is unclear and guessing would create a broader system, choose the narrower option or ask Andrea
