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

The repository is no longer docs-only.

Current shape:

- One Android app module
- Kotlin
- AppCompat
- XML layout
- Single activity
- Minimal dependency set

Current implementation center:

- `app/src/main/java/com/alauncher/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`

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
