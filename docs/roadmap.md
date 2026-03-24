# Roadmap

## Use This File For

- Scope decisions
- v0 versus v1 boundaries
- Questions about whether a requested change should be implemented now

Read this after `docs/index.md` when deciding whether a feature belongs in the current scope.

This is a scope-control document for Copilot guidance, not a feature wish list.

Warning:
scope creep is the default failure mode for this project.
Every addition must justify its existence.

## Copilot Interpretation

- Treat unspecified implementation requests as v0 by default
- Treat v1 as discussion material until a change is explicitly requested
- Do not use the roadmap as permission to pre-build optional systems

## v0

Minimal working launcher.
Strict scope.

- Single home screen
- Vertical app list
- Favorites at top
- Launch installed apps
- Basic search through a system intent
- Default Android behavior only
- No widgets
- No folders
- No settings UI
- No custom animation work

v0 is done when the launcher is usable, stable, and visually sparse.

## v1

Optional extensions.
Not commitments.

Only consider these after v0 is stable and real usage proves a need:

- Simpler favorites management if code-only setup becomes friction
- Small search improvements if system intent is not enough
- Minor polish that improves speed or clarity without expanding scope

If an item adds product surface area, it should probably be rejected.
