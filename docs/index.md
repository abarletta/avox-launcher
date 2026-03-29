# Docs Index

Read this file first for any repository-aware Copilot task.

Its purpose is to tell Copilot exactly where to look in `docs/` before answering.

## Artifact Map

- `docs/context.md`
  - Owner preferences
  - Communication style
  - Current codebase state
  - Use when the question is about project context, loaded context, or how Copilot should adapt

- `docs/architecture.md`
  - System shape
  - Implementation boundaries
  - File and layering constraints
  - Use before implementation, refactoring, or architecture answers

- `docs/roadmap.md`
  - v0 versus v1 scope
  - What is explicitly in or out of scope
  - Use when deciding whether a change belongs in the current product scope

- `docs/ai_roles.md`
  - Copilot model routing
  - Task-to-model guidance
  - Use when the question is about model choice, reasoning depth, or how Copilot should split work

- `docs/copilot-models.json`
  - Detailed model capabilities, tags, context limits, and quota categories
  - Use when model selection needs concrete detail instead of the summary in `docs/ai_roles.md`

- `docs/visual_code_map.md`
  - Visual ownership map for the launcher UI
  - Use when changing layout, icons, typography, widget chrome, settings screen visuals, or animations

- `docs/feature_wishlist.md`
  - Feature ideas with implementation status
  - Use when the question is about planned or potential features

- `docs/missing_features.md`
  - Feature checklist with implementation status and clarification history
  - Use when the question is about what is implemented versus deferred

- `docs/localization_add_language.md`
  - Step-by-step guide for adding a new language translation
  - Use when adding locale support or onboarding contributors for translations

- `docs/known_issues.md`
  - Known artifact/code inconsistencies and technical debt
  - Use when triaging bugs, reviewing configuration, or assessing project health

## Required Read Sets

- For repository context or loaded-context questions:
  - Read `docs/context.md`
  - Read `docs/architecture.md`
  - Read `docs/roadmap.md`
  - Read `docs/ai_roles.md`

- For coding, review, or refactoring tasks:
  - Read `docs/context.md`
  - Read `docs/architecture.md`
  - Read `docs/roadmap.md`

- For prompt or instruction work:
  - Read `docs/ai_roles.md`
  - Read `docs/copilot-models.json` if model specifics matter

## Disclosure Rule

If asked what context is loaded or which artifacts were read:

- Report `docs/index.md` if it was read
- Report each `docs/` file by exact path
- Report which relevant `docs/` files were not read yet
- Do not substitute a code-file summary for an artifact list
