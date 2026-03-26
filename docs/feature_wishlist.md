# Feature Wishlist

This is a list of features that the repository owner would like to implement in the future.

Items are added without any particular order, and without consideration for their complexity or feasibility. The list is meant to be a brainstorming space for potential features, not a roadmap or backlog.

---

## A — Settings Backup and Restore

### Description
Implement a feature to back up and restore user settings. This allows users to transfer settings to a new device or restore them after a factory reset.

### Implementation Details
- Backup stored locally, but easily shareable via built‑in sharing options.
- Restore process should be simple and user‑friendly.
- *Optional:* Backup file may be human‑readable (e.g., JSON) to allow manual editing.

### Status
Partially implemented in commit [15e8258c01bd3f89d203592dd2066248529f10c4](https://github.com/abarletta/a-launcher/commit/15e8258c01bd3f89d203592dd2066248529f10c4). More details in [Audit report on partial implementation of features A, B, and C](feature_abc_partial_implementation.md).

---

## B — Access Settings via Long‑Press on Home Screen

### Description
Allow users to access the settings menu by long‑pressing on an empty area of the home screen.

### Implementation Details
None. This is a common launcher feature and improves accessibility.

### Status
Implemented in commit [216a00269c92037813d7faec7a5f627265ee5a2f](https://github.com/abarletta/a-launcher/commit/216a00269c92037813d7faec7a5f627265ee5a2f)

---

## C — Add Quick Actions to Home Footer

### Description
Add up to 3 customizable quick‑action icons to the home screen footer. Currently, there is a single static icon that opens launcher settings.

### Implementation Details
- Users may add 0–3 quick‑action icons.
- Must be implemented **after Feature B**, since removing the static settings icon would otherwise remove access to settings.
- Quick actions should be customizable from a list of common actions (e.g., launcher settings, system settings, open app, open system pages).
- If phone or messaging apps are selected, notification badges or full‑text notifications should follow the same logic as favorites — but controlled by a separate setting.

### Status
Partially implemented in:
- Commit [15e8258c01bd3f89d203592dd2066248529f10c4](https://github.com/abarletta/a-launcher/commit/15e8258c01bd3f89d203592dd2066248529f10c4). More details in [Audit report on partial implementation of features A, B, and C](feature_abc_partial_implementation.md).
- Commit [710a44787d11319a8fe5ff31b9bc1761b6515cd6](https://github.com/abarletta/a-launcher/commit/710a44787d11319a8fe5ff31b9bc1761b6515cd6). Add launcher settings and widgets home XML layouts and associated images.


---

## D — Multi‑Widget Support

### Description
Allow users to add multiple widgets within the same widget slot and switch between them with a swipe gesture.

### Implementation Details
- Users can add multiple widgets to the same slot and swipe left/right to switch.
- Requires redesign of widget management and corresponding settings UI.

### Status
Not started.

---

## E — Multi‑Column Favorites

### Description
Allow users to choose between single‑column or multi‑column layouts for the favorites section.

### Implementation Details
- Users choose between single‑column and multi‑column layouts.
- Multi‑column layout should adapt to screen size and orientation.
- Reasonable maximum columns (e.g., 2 on phones, 4 on tablets).
- More columns allowed in horizontal layout.

### Status
Not started.

## F — Better Support for Horizontal Layout

### Description
Horizontal layout exists but is below acceptable functionality.

### Implementation Details
- Alphabet must be fully visible without overlapping.
- Widgets must resize to remain fully visible and functional.
- Additional improvements may include optimizing margins and auto‑adjusting favorites layout.
- Depends naturally on Feature E (multi‑column favorites).

### Status
Not started.

---

## G — UI/UX Improvements and Polish

### Description
General UI/UX improvements to enhance the launcher experience:
- Smoother alphabet scrolling animations.
- Smoother transitions between home and app drawer (e.g., animated alphabet expansion, fade effects).
- Settings menu redesign using outline icons and minimal design.
- Wallpaper blur effect should work and be adjustable.
- Wallpaper tint effect should have proper intensity control.
- Favorites and widget selection menus should have improved visual style.

### Implementation Details
- Animation logic should be refactored for easy modification.
- Visual design should be centralized and modular (CSS‑like structure).
- Refactor codebase to make UI/UX improvements easy for human developers.
- Generate documentation explaining where and how to modify animations and styles.

### Status
Not started.
