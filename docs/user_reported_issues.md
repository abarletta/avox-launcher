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


## 1 - Widgets (major)
1. Widgets continue to not load after being added to the home screen. This is NOT related to notification access, as that is correclty requested and granted. See `docs\screenshots\widget_failure.jpg` for a visual example of the issue.
2. Widgets should only appear in the home screen. They should not be visible when the app drawer is open. This is major UI/UX issue.
3. Widget resizing does not work as expected. Long-pressing a widget does not produce any effect. This could be a consequence of the first issue, but it is worth investigating separately.

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