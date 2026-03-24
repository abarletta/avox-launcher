# Roadmap

## Use This File For

- Scope decisions
- Current baseline versus future candidates
- Questions about whether a requested change should be implemented now

Read this after `docs/index.md` when deciding whether a feature belongs in the current scope.

This is a scope-control document for Copilot guidance, not a feature wish list.

Warning:
scope creep is the default failure mode for this project.
Every addition must justify its existence.

## Copilot Interpretation

- Treat the current implementation as the baseline
- Treat future candidates as discussion material until explicitly requested
- Do not use the roadmap as permission to pre-build optional systems

## Current Baseline

The launcher is post-v0 with the following implemented features:

- Single home screen with vertical app list
- Favorites at top
- Widget host (add/bind/configure/restore/resize)
- Full settings activity
- Wallpaper effects (darken, blur, color tint)
- Notification badges and inline text
- Swipe-to-dismiss notifications
- Three sidebar animation styles (wave, highlight, fade)
- Content alignment, margins, spacing controls
- Font selection (system fonts + custom TTF)
- Icon size control
- Third-party icon pack support
- Nerd font icon prefixes
- App long-press actions (info, store, uninstall, shortcuts)
- Local app search
- Settings screen with wallpaper-matched background

All user-reported issues from `docs/user_reported_issues.md` are resolved.

## Future Candidates

Not commitments. Only consider after real usage proves a need:

- Widget management improvements (reorder, per-widget settings)
- Additional wallpaper effects
- Per-app Nerd font glyph customization UI
- Search improvements beyond local filtering
- Accessibility improvements
- Performance profiling and optimization

If an item adds product surface area, it should probably be rejected.
