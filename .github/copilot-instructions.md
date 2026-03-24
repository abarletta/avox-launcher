# A-Launcher Copilot Instructions

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
- Main implementation lives in `app/src/main/java/com/alauncher/MainActivity.kt`
- Main screen layout lives in `app/src/main/res/layout/activity_main.xml`
- Strings and theme live in `app/src/main/res/values/`
- No fragments, no Compose, no Navigation Component, no dependency injection, no extra modules

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
- Prefer changes in `MainActivity.kt`, `activity_main.xml`, `strings.xml`, and `themes.xml`
- Keep app loading, sorting, favorites ordering, and search behavior close to the launcher screen unless complexity forces separation
- Use direct data flow and the smallest necessary state
- Do not move to new layers just to make the code look more "architected"

Stop condition:
- If the feature works and the code is still simple, stop

---

## Architecture Guardrails

- One screen only
- Vertical app list
- Favorites at the top
- Search uses platform APIs first
- Default Android behavior wherever possible
- No widgets
- No folders
- No settings UI
- No custom animations

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

- Treat v0 as strict scope: single home screen, vertical app list, favorites at top, launch installed apps, basic search through a system intent, default Android behavior only
- v0 excludes widgets, folders, settings UI, custom animation work, theming systems, feed pages, and recommendation systems
- v1 items are evaluation candidates, not commitments
- If an item adds product surface area, it should probably be rejected or deferred
