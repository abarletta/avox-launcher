# Feature Wishlist

This is a list of features that the repository owner would like to implement in the future.

Items are added without any particular order, and without consideration for their complexity or feasibility. The list is meant to be a brainstorming space for potential features, not a roadmap or backlog.

| Feature | Description | Status | Last Commit |
| --- | --- | --- | --- |
| [A](#a--settings-backup-and-restore) | Settings Backup and Restore | Implemented | [47f41fc](https://github.com/abarletta/avox-launcher/commit/47f41fc1d0ac47db6d59935903f660753f2df198) |
| [B](#b--access-settings-via-long-press-on-home-screen) | Access Settings via Long‑Press on Home Screen | Implemented | [216a002](https://github.com/abarletta/avox-launcher/commit/216a00269c92037813d7faec7a5f627265ee5a2f) |
| [C](#c--add-quick-actions-to-home-footer) | Add Quick Actions to Home Footer | Implemented | [5c69698](https://github.com/abarletta/avox-launcher/commit/5c69698a0f4164f3b284630d0cd73771e269cd8a) |
| [D](#d--multi-widget-support) | Multi‑Widget Support | Implemented | [9ec62de](https://github.com/abarletta/avox-launcher/commit/9ec62de9dcd5aa615332aefec9ac2368d3d62b08) |
| [E](#e--multi-column-favorites) | Multi‑Column Favorites | Implemented | [58e00f4](https://github.com/abarletta/avox-launcher/commit/58e00f42eaab89e0320644f19126b198581c01c3) |
| [F](#f--better-support-for-horizontal-layout) | Better Support for Horizontal Layout | Not started | |
| [G](#g--uiux-improvements-and-polish) | UI/UX Improvements and Polish | In progress | [9a6b299](https://github.com/abarletta/avox-launcher/commit/9a6b299c0057ed5ab4c71eff1638329617686a51) |
| [H](#h--advanced-controls-for-favorites-and-widgets) | Advanced controls for favorites and widgets | Implemented | [7848c68](https://github.com/abarletta/avox-launcher/commit/7848c680a568476774f2f042daebdd82b8625e12) |
| [I](#i--multilangual-support) | Multilangual support | Not started | |  
| [J](#j--better-icon-pack-support) | Better Icon Pack Support | Not started | |

---

## A — Settings Backup and Restore

### Description
Implement a feature to back up and restore user settings. This allows users to transfer settings to a new device or restore them after a factory reset.

### Implementation Details
- Backup stored locally, but easily shareable via built‑in sharing options.
- Restore process should be simple and user‑friendly.
- *Optional:* Backup file may be human‑readable (e.g., JSON) to allow manual editing.

### Status
Implemented in commit [47f41fc1d0ac47db6d59935903f660753f2df198](https://github.com/abarletta/avox-launcher/commit/47f41fc1d0ac47db6d59935903f660753f2df198).

---

## B — Access Settings via Long‑Press on Home Screen

### Description
Allow users to access the settings menu by long‑pressing on an empty area of the home screen.

### Implementation Details
None. This is a common launcher feature and improves accessibility.

### Status
Implemented in commit [216a00269c92037813d7faec7a5f627265ee5a2f](https://github.com/abarletta/avox-launcher/commit/216a00269c92037813d7faec7a5f627265ee5a2f)

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
Implemented in commit [5c69698a0f4164f3b284630d0cd73771e269cd8a](https://github.com/abarletta/avox-launcher/commit/5c69698a0f4164f3b284630d0cd73771e269cd8a).

---

## D — Multi‑Widget Support

### Description
Allow users to add multiple widgets within the same widget slot and switch between them with a swipe gesture.

### Implementation Details
- Users can add multiple widgets to the same slot and swipe left/right to switch.
- Requires redesign of widget management and corresponding settings UI.

### Status
Implemented in commit [9ec62de9dcd5aa615332aefec9ac2368d3d62b08](https://github.com/abarletta/avox-launcher/commit/9ec62de9dcd5aa615332aefec9ac2368d3d62b08).

---

## E — Multi‑Column Favorites

### Description
Add multi-column layout for favorites slot in home screen.

### Implementation Details
- Users choose between single‑column and multi‑column layouts.
- Multi‑column layout should adapt to screen size and orientation.
- Reasonable maximum columns (e.g., 2 on phones, 4 on tablets).
- Increase maximum columns in horizontal layout (e.g., 4 on phones, 6 on tablets).
- Once multi‑column favorites are implemented, set home screen layout to default to 2 columns in portrait and 4 columns in landscape. The current single-column layout will be available as an option in settings, but it will no longer be the default.

### Status
Implemented in commit [58e00f42eaab89e0320644f19126b198581c01c3](https://github.com/abarletta/avox-launcher/commit/58e00f42eaab89e0320644f19126b198581c01c3).

## F — Better Support for Horizontal Layout

### Description
Horizontal layout exists but is below acceptable functionality.

### Implementation Details
- Alphabet must be fully visible without overlapping.
- Widgets must resize to remain fully visible and functional.
- Additional improvements should include optimizing margins, multi-column widget slots (stacking widgets in same slot horizontally), and auto‑adjusting favorites layout.
- Depends naturally on Feature E (multi‑column favorites).

### Status
Not started.

---

## G — UI/UX Improvements and Polish

### Description
General UI/UX improvements to enhance the launcher experience:
- <del>Smoother alphabet scrolling animations.</del>
  >Implemented in commit [9a6b299](https://github.com/abarletta/avox-launcher/commit/9a6b299c0057ed5ab4c71eff1638329617686a51)
- Smoother transitions between home and app drawer (e.g., animated alphabet expansion, fade effects).
- Wallpaper blur effect should work and be adjustable.
- Wallpaper tint effect should have proper intensity control.
- Overall visual design in settings, long-press menus, and other UI elements should be polished and consistent, along the following guidelines:
    - Consistent font (font chosen in settings should be applied everywhere).
    - Consistent color scheme (e.g., for text, icons, backgrounds).
    - For buttons and interactive elements, prefer text-only design instead of buttons with visible boxes.

### Implementation Details
- Animation logic should be refactored for easy modification.
- Visual design should be centralized and modular (CSS‑like structure).
- Refactor coebase to make UI/UX improvements easy for human developers.
- Generate documentation explaining where and how to modify animations and styles.

### Status
In progress.

## H - Advanced Controls for Favorites and Widgets

### Description
Provide more advanced controls for favorites and widgets, such as:
- Seeing actual favorites in the settings menu.
- Reordering favorites from the settings menu. 
- Include actual app icons in the favorites picker, instead of just a list of app names.
- In the widget settings, replace the "Remove" and "Controls" text-based buttons next to each widget with icons.
- Consider removing the "Home Screen Rows" setting under "Appearance" unless it has an actual effect on the home screen layout. Currently, the setting does not seem to have any effect from a user-facing perspective. The reason why this request is included here is that this setting should be related with widgets.
- Optionally, favorite and widget reordering may also be allowed via drag‑and‑drop on the home screen.

### Implementation Details
- Favorites management UI should be redesigned to accommodate these features.
- Drag‑and‑drop reordering on the home screen should be considered an optional enhancement. As such, it should only be implemented after the core features of favorites management are completed and stable.

### Status
Implemented in commit [7848c680a568476774f2f042daebdd82b8625e12](https://github.com/abarletta/avox-launcher/commit/7848c680a568476774f2f042daebdd82b8625e12).

## I - Multilangual support

### Description
Add support for multiple languages in the launcher, allowing users to select their preferred language from the settings menu.

### Implementation Details
- Start by externalizing all user-facing text into resource files. This will require a deep scan of the codebase to identify and extract hardcoded strings.
- Implement a language selection option in the settings menu. The default language will be the system language if supported, or English if not. Users can manually select their preferred language from a list of available options.
- English will be the only language at launch, but the infrastructure should allow for easy addition of new languages in the future.
- Given the availability of AI, adding languages in the future should be straightforward and not require significant development effort. The main challenge will be ensuring that the UI can accommodate different text lengths and character sets.


### Status
Not started.

## J — Improved Icon Customization

### Description
Improve icon customization and ensure consistent icon rendering across the entire launcher. Users should be able to override icons on a per‑app basis, and the launcher should apply a unified icon‑resolution pipeline everywhere (home screen, app drawer, favorites, quick actions, etc.). Add minimal built‑in icon packs to provide clean fallback icons and simple aesthetic themes.

### Implementation Details
- Introduce a unified icon‑resolution system with priority:
  user‑customized icon → third‑party icon pack → built‑in pack → default app icon.
- Add per‑app icon overrides via the long‑press menu; overrides apply globally across the launcher.
- Ensure all UI surfaces use the same icon pipeline for consistent theming.
- Add minimal built‑in icon packs with multiple styles (full, outline, rounded, square) and color themes (dark, light, blue, red, green, yellow, violet).
- Built‑in packs provide generic category icons to fill gaps in third‑party packs. Required categories:
  1. phone
  2. message
  3. contacts
  4. email
  5. shopping
  6. finance
  7. document
  8. calendar
  9. settings
  10. gamepad
  11. tools
  12. health
  13. travel/luggage
  14. music
  15. movie/video
  16. education
  17. gallery/photos
  18. baby/parenting
  19. security/locker
  20. parcel/delivery
  21. password/credentials
  22. web browser
  23. file manager
  24. camera
  25. social media
  26. bolt/flash
  27. star
  28. heart
  29. cloud
  30. eye/view
  31. food
  32. maps/navigation
  33. news/reading
  34. sports/activity

### Status
Not started.

## K - Gesture Support

### Description
Add support for customizable gestures on the home screen and app drawer, allowing users to trigger specific actions (e.g., open app drawer, open notifications, launch specific apps) with swipe or tap gestures.

### Implementation Details
- This should extend the current long-press functionality to:
  1. Customize the long-press action on the home screen (currently opens the launcher settings).
  2. Add new gestures (e.g., swipe up, swipe down, double tap) that can be assigned to different actions.
- Optionally, and only after careful consideration of the added complexity, available actions could include screen lock. This requires the app to request the `DEVICE_ADMIN` permission.
