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

The launcher is post initial scope with the following implemented features:

- Single home screen with vertical app list (section headers in expanded view)
- Favorites at top with multi-column grid layout (adaptive columns by screen/orientation)
- Widget host (add/bind/configure/restore/resize)
- Multi-widget slots with horizontal swipe between widgets
- Full settings activity with fragment-based navigation
- Wallpaper effects (darken, color tint)
- Notification badges and inline text
- Swipe-to-dismiss notifications
- Three sidebar animation styles (wave, highlight, fade)
- Content alignment (entire row), margins (h: 0–120dp), spacing controls
- Sidebar position synced with app list
- Font selection (system fonts + custom TTF)
- Icon size control
- Third-party icon pack support
- Nerd font icon prefixes
- App long-press actions (info, store, uninstall, shortcuts)
- Local app search
- Quick actions footer (up to 3 customizable slots)
- Settings backup and restore (JSON export/import)
- Language selection with 11 locale translations (da, de, es, fi, fr, it, nb, nl, pl, pt, sv)
- Home screen long-press to open settings
- Settings screen with wallpaper-matched background

All user-reported issues from `docs/user_reported_issues.md` are resolved.

Widget management is available both from Settings (primary) and from the home screen via long-press edit mode.

## Future Candidates

Not commitments. Only consider after real usage proves a need:

- Widget management improvements (reorder, per-widget settings)
- Additional wallpaper effects
- Per-app Nerd font glyph customization UI
- Search improvements beyond local filtering
- Accessibility improvements
- Performance profiling and optimization

If an item adds product surface area, it should probably be rejected.
