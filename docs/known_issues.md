# Known Issues

Status updated on 2026-04-11 based on user feedback and internal testing.

## 1 - App version number overlaps with navigation bar on devices with a navigation bar

Status: Fixed in code on 2026-04-11 for final release prep.

### Description
The app version number in the settings menu overlaps with the navigation bar when present. This wasn't detected before because the reference device used for testing didn't have a navigation bar and the version number is a recent addition to the app. This issue affects the visibility of the version number and can make it difficult for users to read it.

### Resolution
The settings menu now applies the navigation bar inset plus the existing quick actions bottom offset to the scroll container, which keeps the version label above the system bar without touching localized string resources.

## 2 - Wallpaper selection has no effect on the home screen

Status: Fixed in code on 2026-04-11 for final release prep.

### Description
When users select a wallpaper, it does not update on the home screen. The only way to change the wallpaper is to set it through the system settings on both Samsung and Pixel devices.

### Resolution
Wallpaper selection now requests the required wallpaper-setting permission in the manifest and applies the picked image through WallpaperManager.FLAG_SYSTEM so the launcher updates the home wallpaper directly instead of failing silently.

## 3 - Wallpaper overlay "blurring" effect does not work

### Description
The wallpaper overlay effect that is supposed to blur the wallpaper does not work as intended. The wallpaper remains clear and unblurred regardless of the selected overlay effect.

### Proposed fix
Investigate the implementation of the wallpaper overlay effect and ensure that the blurring effect is applied correctly. This may involve checking the rendering logic, verifying that the correct APIs are being used, and testing on multiple devices to confirm that the issue is resolved.

Note that this is a long-standing issue that has been present since the initial implementation of the wallpaper overlay effects. Consider simply removing this functionality if it is deemed too complex or time-consuming to fix, as it is not a critical feature and the other effects (darkening and color overlay) are working correctly.