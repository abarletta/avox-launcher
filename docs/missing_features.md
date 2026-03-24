# Features

## Search functionality
- [x] Local search bar for filtering installed apps.

## Widget support
- [x] Support for third-party widgets (add, bind, configure, restore, resize).
- [ ] Basic built-in widgets (e.g., clock and media player). — Deferred.

## App controls
- [x] Quick actions for apps (long-press: app info, Play Store, uninstall, shortcuts).

## Display icons next to app names
- [x] Show app icons next to app names with configurable icon size.
- [x] Third-party icon pack support with safe fallback to system icons.
- [x] Nerd font icon prefixes for common apps (user-provided Nerd font TTF).

## Notification badges
- [x] Display notification badges (count mode) on app icons.
- [x] Display notification text (text mode) from apps.
- [x] Swipe-to-dismiss notifications (left swipe) and quick actions (right swipe).

## Customization options
- [x] Custom UI themes (light, dark, follow system).
- [x] Customizable app icons from third-party icon packs.
- [x] Customizable font size.
- [x] Customizable fonts from TTF files.
- [x] Wallpaper overlay effects: darken, blur, and color tint with per-effect controls.
- [x] Wallpaper selection control.
- [x] Icon size control.

## Animations
- [x] Three sidebar animation styles: wave/zoom, highlight, fade.
- [x] Per-style controls: wave shift/scale, highlight intensity, fade radius.
- [ ] Smooth animations for opening/closing apps and widget interactions. — Deferred.


# Clarifications
## 1 - Search
### Question:
Do you want an in-app search bar that filters the installed apps list (fast, local), or a system/global search (intent) that can also include contacts/settings? Or both (UI + system intent fallback)?

### Answer:
Only apps should be in scope for now. Therefore, please proceed with implementing a local search bar that filters the installed apps list.

## 2 - Widgets
### Question:
Earlier you put widget support on hold. Should I add only the placeholder code/APIs to host widgets now, or fully implement third-party widget support?

### Answer:
This is a follow-up to the earlier request to put widget support on hold. Please implement full support for third-party widgets now. You may replace this task at the end of the checklist if you assess it to be too complex or time-consuming. However, it should be part of this task.

## 3 - Icons
### Question:
Earlier you asked for "app names only for now." Should I implement icons now (from apps' own icons), or keep names-only and defer icons to a later step?

### Answer:
This is a follow-up to the earlier request you mentioned about "app names only for now." Please proceed with implementing icons now as per instructions above.

## 4 - Notification badges
### Question:
This requires adding a NotificationListenerService and instructing the user to grant the Notification Access permission. Are you OK with the required permission and UX prompt? Badge behavior: simple unread-count per package, or detailed notifications expanded?

### Answer:
I am OK with the required permission and UX prompt for the Notification Access permission. 

Implement BOTH simple unread-count per package and detailed notifications expanded. Allow user to choose between:
- Simple unread-count per package (default)
- Detailed notifications expanded (shows notification text from apps that support it, e.g., messaging apps). This should match the target launcher's behavior (see screenshots).
- No badges (hide badges entirely).

## 5 - App controls (long-press)
### Question:
Which actions do you want on long-press? (e.g., App info, Uninstall, Shortcuts if available). I can implement a small bottom-sheet offering App info + Shortcuts.

### Answer:
As a minimum, please provide a pop-up menu on long-press that offers the following actions:
- App info and link to play store (if available)
- Uninstall (if available)
- Shortcuts (if available)

You can use the screenshots of the target launcher as visual guidance for the design of the long-press menu. But you do not need to implement all the funcionality shown in the screenshots. The above list of actions is sufficient for now.

## 6 - Customization scope
### Question:
I already added font, spacing, wallpaper darkness and a Settings screen. Confirm you want:
- wallpaper selection UI (select system wallpaper or pick image),
- theme presets (light/dark/system),
- ability to load custom TTF fonts from storage (requires file picker + permission).

### Answer:
In addition to what you listed above, I want:
- Controllable font size (currently missing).
- Custom wallpaper overlay effects (currently only supports darkening, but may add blur and color overlay in the future).

## 7 - Animations
### Question:
Prioritize which (letter highlight, smooth list transitions). Do you want any animation library, or simple built-in animations?

### Answer:
Prioritize matching the target launcher's letter transition animations, as shown in the screenshots. If it is too difficult to add additional animation styles, stop here for now and defer the other styles to a later step.

## 8 - Screenshots
### Question:
Confirm I may use all images there for visual guidance. If any filename is ambiguous, I'll ask.

### Answer:
Please ask about any ambiguous filenames BEFORE starting implementation. If no ambiguities are raised, you may silently start.

