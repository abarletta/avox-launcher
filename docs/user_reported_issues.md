# User-reported issues

## Summary and status

| # | Issue | Severity | Status |
| --- | --- | --- | --- |
| [1](#1---widgets-major) | Widgets | Major | Partially addressed |
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
3. Allow widget reordering from the settings screen. This is a common feature in launchers and it would enhance user customization options.

**Important**: this issue has been around since the initial release and it has survived multiple attempts to fix it. Even consulting the internet and implementing Launcher3 widget management code did not solve the issue. It is possible that the issue is related to a fundamental misunderstanding of the widget system, which may require a more in-depth investigation and potentially a complete refactor of the widget management system. Can it just be related with OEM (Samsung), security or permission issues? The user can confirm that the app is set as default launcher but that no request for permission to create widgets is ever shown.

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
1. Wallpaper overlay effects other than darkening do not work. Setting effects to blur or color filters does not produce any visible effect on the home screen.

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
1. The transition when scrolling through the alphabet letters in the app drawer should be animated (as currently implemented) but instantaneous. Transition to next list should happen as soon as the user scrolls to the next letter, without any delay. The current implementation awaits that the user releases the touch after scrolling to the next letter, which creates a sluggish and unresponsive feeling. The animation should be triggered immediately when the user scrolls to the next letter, and it should not be interrupted by touch events until it completes.
2. The height of the vertical alphabet list is correctly synced with the height of the favorites app list and by the right approach. However, when switching to the app drawer, the alphabet list height is not updated on the first user interaction. It requires two interactions to be correctly synced. The app drawer should be rendered full height at the first touch (without requiring two touches and without even awaiting releasing the touch).
3. When using Nerd icons, additional spacing should be added between the icon and the app name.
4. If not too difficult, there should be an option to hide the status bar on the home screen. It too complex, leave it for a future release.
5. In the settings main screen, icons should be added to each card to make it more visually appealing and easier to navigate.

## 12 - Settings menu refactor (minor)
1. The settings menu is currently implemented as a single activity with a long list of controls. This is not very scalable and it makes the codebase difficult to maintain. It would be better to refactor the settings menu into multiple fragments, each responsible for a specific category of settings (e.g., appearance, behavior, notifications, etc.).
2. The settings menu should have a more polished visual design, with:
   - Slightly increased margins.
   - Fonts aligned with the home screen.
   - Nicer sliders and buttons (currently buttons have a grey solid background regardless of the wallpaper and theme)
   - Animations.
