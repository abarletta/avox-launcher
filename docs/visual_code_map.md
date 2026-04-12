# Visual Code Map

This file maps each visible part of Avox to the code and resources that control it.

## Home Screen Shell

- Main screen layout: `app/src/main/res/layout/activity_main.xml`
  - Controls the root stacking order for wallpaper overlay, widget area, app list, search bar, sidebar, and bottom button.
  - If touch behavior or layering feels wrong, check this file first.
- Main screen behavior: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `onCreate(...)` wires every visual element.
  - `updateBottomButton()` and `updateBottomButtonImmediate()` switch the bottom button icon and show or hide widgets depending on launcher mode.
  - `applySettings()` updates wallpaper dimming and color tint.
  - `applyLayoutPrefs()` updates horizontal margins and widget area sizing.
  - `applyStatusBarPref()` controls immersive status bar visibility.

## Wallpaper Look

- Wallpaper effect settings UI: `app/src/main/java/com/avox/launcher/SettingsWallpaperFragment.kt`
  - Controls theme mode, wallpaper effect type, darkness, and color tint preferences.
- Wallpaper settings layout: `app/src/main/res/layout/fragment_settings_wallpaper.xml`
  - Defines the visible controls for theme and wallpaper options.
- Live wallpaper overlay rendering: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `applySettings()` reads the saved wallpaper preferences and applies either darkening or color tint to `darkOverlay`.

## App List Rows

- Row layout: `app/src/main/res/layout/item_app.xml`
  - Controls row structure, icon slot size, app name text, and inline notification text placement.
- Row binding and styling: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `refreshList()` collects font, spacing, font size, notification mode, alignment, icon size, icon pack, and icon mode, then rebuilds the adapter.
  - `AppListAdapter` applies those values to each row at bind time.
  - Notification badges and preview text are also composed in the adapter.

## Favorites Grid

- Grid layout: `app/src/main/res/layout/item_favorite_grid.xml`
  - Controls the grid cell structure with icon and notification badge.
- Grid container: `app/src/main/res/layout/activity_main.xml`
  - `favoritesGrid` is the GridView above the app list for multi-column favorites.
- Grid behavior: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `refreshList()` decides whether to use the favorites grid (adaptive layout) or inline favorites in the app list.
  - `FavoriteGridAdapter` binds favorite apps to grid cells with icons, badges, and click/long-press handling.
  - `resolveFavoritesColumnCount()` computes adaptive column count based on screen size and orientation.
- Favorites management: `app/src/main/java/com/avox/launcher/SettingsSystemFragment.kt`
  - Favorites picker with app icons, reordering via up/down controls.

## Icons

- Launcher row icons: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `refreshList()` decides whether to use regular app icons, nerd-font icons, or no icons.
  - Icon pack loading is mediated through `IconPackResolver`.
- Appearance controls for icon mode, icon size, icon pack, and custom fonts: `app/src/main/java/com/avox/launcher/SettingsAppearanceFragment.kt`
- Vector/icon drawable resources: `app/src/main/res/drawable/`
  - `ic_settings.xml`: bottom settings button icon.
  - `ic_search.xml`: bottom search button icon.
  - `ic_delete.xml`: widget edit remove button icon.
  - `ic_shortcut.xml`: fallback shortcut icon in the app actions sheet.
  - `circle_dark_bg.xml`: circular dark button background.
  - `badge_bg.xml`: notification badge background.

## Typography And Spacing

- Appearance controls: `app/src/main/java/com/avox/launcher/SettingsAppearanceFragment.kt`
  - Owns font family, custom TTF upload, font size, row spacing, alignment, margins, block count, icon mode, icon size, icon pack, and status bar visibility preferences.
- Runtime application of typography and spacing: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `refreshList()` applies font family, font size, row spacing, alignment, and icon sizing to the launcher rows.
  - `resolveTypeface(...)` loads either system fonts or the custom uploaded font.
  - `applyLayoutPrefs()` updates screen margins and widget-area sizing limits.

## Widgets

- Widget visual container and screen placement: `app/src/main/res/layout/activity_main.xml`
  - `widgetContainer` is the vertical host area above the app list.
- Widget host behavior and edit UI: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `createWidgetWrapper(...)` defines the wrapper, edit buttons, resize handle, full-width application, and saved height behavior.
  - `enterWidgetEditMode()` and `exitWidgetEditMode()` show or hide widget editing chrome.
  - `applyWidgetSize(...)`, `refreshWidgetSizes()`, and `updateListPaddingForWidgets()` control widget size and surrounding layout.
  - `WidgetFrame` is the wrapper view responsible for launcher-side widget long-press entry into edit mode.

## Sidebar And Motion

- Sidebar rendering and touch animation: `app/src/main/java/com/avox/launcher/AlphabetSidebar.kt`
  - `drawWave(...)`, `drawHighlight(...)`, and `drawFade(...)` implement the three sidebar visual styles.
  - `animateIn()` and `animateOut()` drive the sidebar motion envelope.
  - Font family also affects sidebar text via `setFontFamily(...)`.
- Sidebar settings UI: `app/src/main/java/com/avox/launcher/SettingsAnimationsFragment.kt`
  - Controls animation style, wave shift, wave scale, highlight intensity, and fade radius.
- List crossfade when switching content: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `animateListTransition()` handles the launcher list fade-out/fade-in.

## Bottom Button And Search Bar

- Layout placement: `app/src/main/res/layout/activity_main.xml`
  - `bottomButton` controls the centered floating action button.
  - `searchBar` defines the inline search field.
- Behavior and icon switching: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - `updateBottomButton()` swaps between settings and search states.
  - `showSearchBar()` and `hideSearchBar()` control the search field visibility and keyboard.

## Quick Actions Footer

- Action slot model: `app/src/main/java/com/avox/launcher/LauncherQuickActions.kt`
  - Manages up to 3 footer action slots with system/app intents (launcher settings, wifi, bluetooth, display, apps, system settings, pick custom app).
  - `resolveFooterAction()` builds the intent and icon for each slot.
- Footer rendering: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - Footer action views are built dynamically from `LauncherQuickActions` at refresh time.
- Quick action configuration: `app/src/main/java/com/avox/launcher/SettingsSystemFragment.kt`
  - Quick actions section lets users assign actions to each footer slot.

## Widget Picker

- Picker item layout: `app/src/main/res/layout/item_widget_picker.xml`
  - Displays widget app name, widget name, dimensions text, and preview icon.
- Picker header layout: `app/src/main/res/layout/item_widget_picker_header.xml`
  - Category separator for grouping widgets by source app.
- Picker behavior: `app/src/main/java/com/avox/launcher/MainActivity.kt`
  - Widget picker dialog built with list adapter for widget selection.

## Settings Screens

- Settings activity shell: `app/src/main/java/com/avox/launcher/SettingsActivity.kt`
  - Hosts the settings fragments and applies slide transitions between them.
- Settings activity layout: `app/src/main/res/layout/activity_settings.xml`
  - Defines the dark overlay and fragment container.
- Top-level settings menu: `app/src/main/res/layout/fragment_settings_menu.xml`
  - Controls the card look, emoji icons, labels, and descriptions for each settings entry.
- Menu routing: `app/src/main/java/com/avox/launcher/SettingsMenuFragment.kt`
  - Decides which fragment opens for each card.
- Notifications and widget/home settings screen: `app/src/main/res/layout/fragment_settings_system.xml`
  - Controls the visible sections for notifications versus widgets/home management.
- Notifications and widget/home logic: `app/src/main/java/com/avox/launcher/SettingsSystemFragment.kt`
  - Chooses which section is visible and builds the widget-management rows, including remove, full-width, and widget-control actions.

## App Actions Sheet

- Bottom sheet behavior: `app/src/main/java/com/avox/launcher/AppActionsSheet.kt`
  - Controls app info, Play Store, uninstall, and dynamic shortcuts UI.
- Bottom sheet layouts: `app/src/main/res/layout/sheet_app_actions.xml` and `app/src/main/res/layout/item_shortcut.xml`
  - Define the sheet structure and shortcut row visuals.

## Fastest Edit Paths

- Want to move or restack launcher elements: edit `app/src/main/res/layout/activity_main.xml`.
- Want to change app row visuals: edit `app/src/main/res/layout/item_app.xml` and `app/src/main/java/com/avox/launcher/MainActivity.kt`.
- Want to change favorites grid visuals: edit `app/src/main/res/layout/item_favorite_grid.xml` and `app/src/main/java/com/avox/launcher/MainActivity.kt`.
- Want to change wallpaper look: edit `app/src/main/java/com/avox/launcher/SettingsWallpaperFragment.kt` and `app/src/main/java/com/avox/launcher/MainActivity.kt`.
- Want to change sidebar animation: edit `app/src/main/java/com/avox/launcher/AlphabetSidebar.kt`.
- Want to change settings menu card appearance: edit `app/src/main/res/layout/fragment_settings_menu.xml`.
- Want to change widget edit visuals: edit `app/src/main/java/com/avox/launcher/MainActivity.kt` inside `createWidgetWrapper(...)`, `enterWidgetEditMode()`, and `exitWidgetEditMode()`.
- Want to change footer quick actions: edit `app/src/main/java/com/avox/launcher/LauncherQuickActions.kt` for action types, `app/src/main/java/com/avox/launcher/MainActivity.kt` for rendering.
- Want to add a new language: follow `docs/localization_add_language.md`.