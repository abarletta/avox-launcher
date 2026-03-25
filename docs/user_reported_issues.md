# User-reported issues

## Summary and status

| # | Issue | Severity | Status |
| --- | --- | --- | --- |
| [1](#1---widgets-major) | Widgets | Major | Resolved |
| [2](#2---horizontal-alignment-major) | Horizontal alignment | Major | Resolved |
| [3](#3---font-selector-major) | Font selector | Major | Resolved |
| [4](#4---app-controls-on-long-press-medium) | App controls on long-press | Medium | Resolved |
| [5](#5---letter-transition-animations-medium) | Letter transition animations | Medium | Resolved |
| [6](#6---vertical-alignment-minor) | Vertical alignment | Minor | Resolved |
| [7](#7---missing-wallpaper-overlay-effects-minor) | Missing wallpaper overlay effects | Minor | Resolved |
| [8](#8---notification-text-minor) | Notification text | Minor | Resolved |
| [9](#9---app-icon-size-minor) | App icon size | Minor | Resolved |
| [10](#10---settings-screen-minor) | Settings screen | Minor | Resolved |
| [11](#11---uiux-polish-minor) | UI/UX polish | Minor | Resolved |
| [12](#12---settings-menu-refactor-minor) | Settings menu refactor | Minor | Resolved |


## 1 - Widgets (major)
1. Widgets continue to not load after being added to the home screen. Only Google widgets work. Non-Google widgets fail to load and display either a "Couldn't add widget" or "Can't show content" error message. 
2. Widget can be resized from the settings screen, but not via long-press edit mode on the home screen. Even more importantly, clicking on widgets (short or long) does not trigger any action.

**Important**: this issue has been around since the initial release and it has survived multiple attempts to fix it. It could be a good idea to search the internet for solutions given that in-house knowledge seems to be insufficient to solve it.

## 2 - Horizontal alignment (major)
1. Changing horizontal alignment does not produce any visible effect.
2. Controls may become unresponsive after setting horizontal alignment to center (unclear, difficult to reproduce).
3. Margins work as expected.

## 3 - Font selector (major)
1. After a custom TTF font is selected, the selector persists through calls to the settings screen. This means that every time the settings are opened, the font selector will appear. Scrolling back to the initial settings screen does not rectify this behavior: when opening the settings again, the font selector will still be the landing screen.

## 4 - App controls on long-press (medium)
1. Pressing "Uninstall" via long-press on an app icon does not trigger the uninstall process.

## 5 - Letter transition animations (medium)
1. Wave shift and effect controls should only appear when the "Wave/Zoom" animation is selected.
2. When "Highlight" or "Fade" animation is selected, relevant controls (e.g., highlight intensity and fade radius) should be made available.

## 6 - Vertical alignment (minor)
1. Vertical margins work as expected but more gap should be allowed (currently only 0-48dp).

## 7 - Missing wallpaper overlay effects (minor)
1. Wallpaper overlay effects other than darkening are not implemented. Blur and color filters should be added as per initial design specifications. When they are selected, the controls for the "darken" effect should be hidden, and the relevant controls for the selected effect should be displayed instead.

## 8 - Notification text (minor)
1. This is just a UI/UX issue. When the "Show notification text" option is enabled, the text is displayed in a small and dimmed font, which is difficult to read. The text should be made larger and more prominent (e.g., bold) to improve readability.
2. If not too complex, it would be nice to add swype gestures to the notification badges, allowing users to quickly clear notifications by swiping left, and trigger quick actions by swiping right.

## 9 - App icon size (minor)
1. App icon size should be controllable via a setting in the app. This is a common feature in launchers and would enhance user customization options.
2. There is currently no support to third-party icon packs. This was agreed to be out of scope for the initial release, but it should be reconsidered unless its complexity is deemed high enough to warrant delayed implementation through a separate targeted task.
3. It could be considered to add a Nerd font option for app icons. To add an icon to an app label, the corresponding Unicode character from the Nerd font set should be added as a prefix to the app name. This would allow users to easily customize their app icons using a wide variety of symbols and icons available in the Nerd font collection. No asset need to be added to the app, as the icons would be rendered using a user-provided font.

## 10 - Settings screen (minor)
1. The settings screen should have same background as the home and app drawer screens. This should replace the current dark/light background behavior.
2. The settings screen should present a less rudimentary design, with nicer sliders and fonts matching the home and app drawer screens.

## 11 - UI/UX polish (minor)
1. The heading letters in the app drawer should transition with an animation when scrolling through the app list. Furthermore, they should be slightly larger and brighter/bolder than they currently are, to make them more visually distinct and easier to read.
2. The height of the vertical alphabet list is correctly synced with the height of the favorites app list. However, this is done by downsizing the alphabet list font size instead of adjusting the vertical spacing between letters. This could be particularly noticeable when the favorites list is very short. This should be fixed by keeping the font size constant and adjusting the vertical spacing between letters to fit the available height.
3. When Nerd icons are enabled, the regular icons should be hidden to avoid visual clutter and confusion. Otherwise, a double icon would be displayed for each app (the regular icon and the Nerd font icon). Ideally, the user should be able to choose between three options: regular icons only, Nerd font icons only, or no icon. Furthermore, the icon size parameter should control the size of both regular and Nerd font icons to maintain visual consistency.
5. The vertical margin setting seems uneffective when there is a widget in the home screen. Furthermore, when there is a widget, there is a gap between the bottom of the widget and the top of the first app, which creates a disjointed visual effect. This gap should be zero if the vertical margin is set to zero, and should increase/decrease according to the vertical margin setting, so to create a more cohesive and visually appealing design. The issue seems to be related with the fact that resizing a widget modifies the height of the widget container, but the app list does not adjust its position accordingly.
6. Animations when transitioning between home and app drawers (both ways) would create a smoother and more polished user experience. For example, a fade or slide animation (or both, up to the user) could be implemented to make the transition less abrupt and more visually appealing.

## 12 - Settings menu refactor (minor)
1. The settings menu is currently implemented as a single activity with a long list of controls. This is not very scalable and it makes the codebase difficult to maintain. It would be better to refactor the settings menu into multiple fragments, each responsible for a specific category of settings (e.g., appearance, behavior, notifications, etc.).
2. The settings menu should have a more polished visual design, with:
   - Slightly increased margins.
   - Fonts aligned with the home screen.
   - Nicer sliders and buttons (currently buttons have a grey solid background regardless of the wallpaper and theme)
   - Animations.
