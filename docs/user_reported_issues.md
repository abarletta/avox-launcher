# User-reported issues

## Summary and status

| # | Issue | Severity | Status |
| --- | --- | --- | --- |
| [1](#1---widgets-major) | Widgets | Major | Unresolved |
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


## 1 - Widgets (major)
1. Widgets continue to not load after being added to the home screen. Only Google widgets work. Non-Google widgets fail to load and display either a "Couldn't add widget" or "Can't show content" error message. 
2. Widget can be resized from the settings screen, but not via long-press edit mode on the home screen. Even more importantly, clicking on widgets (short or long) does not trigger any action.

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
1. The app drawer screen should list the main letter categories (e.g., A, B, C, D, etc.) as section headers to improve navigation and readability. See `docs\screenshots\expanded_view_target.jpg` for a visual example. Note that only the heading letters should be taken as example from the sceenshot, but not the rest of the design which includes displaying apps for other letters, which is not desired.
2. The vertical alphabet list in the main screen should be made more compact, ideally as tall as the favorites app list, so to give the impression of being a single unified sidebar.   
3. The horizontal alignment in the home screen only applies to the app labels, but not to the icons, which creates a disjointed visual effect. The horizontal alignment setting should be applied to the entire app row, including both the icon and the label, to create a more cohesive and visually appealing design.
4. The horizontal margin setting should allow for larger margins (e.g., up to 120dp).
5. The vertical margin setting seems uneffective when there is a widget in the home screen. Furthermore, when there is a widget, there is a gap between the bottom of the widget and the top of the first app, which creates a disjointed visual effect. This gap should be zero if the vertical margin is set to zero, and should increase/decrease according to the vertical margin setting, so to create a more cohesive and visually appealing design. The issue seems to be related with the fact that resizing a widget modifies the height of the widget container, but the app list does not adjust its position accordingly.

