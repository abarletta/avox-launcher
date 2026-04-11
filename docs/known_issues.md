# Known Issues

Status updated on 2026-04-11 based on user feedback and internal testing.

## 1 - App version number overlaps with navigation bar on devices with a navigation bar

### Description
The app version number in the settings menu overlaps with the navigation bar when present. This wasn't detected before because the reference device used for testing didn't have a navigation bar and the version number is a recent addition to the app. This issue affects the visibility of the version number and can make it difficult for users to read it.

### Proposed fix
Modify the layout file for the settings menu to add a bottom margin that is equal to or greater than the height of the navigation bar. If suitable, this could use the same setting used for the quick actions bottom spacing to maintain consistency. This solution also avoids modifying string files, which should not be changed if possible, to avoid the burden of updating translations.

## 2 - Wallpaper selection has no effect on the home screen

### Description
When users select a wallpaper, it does not update on the home screen. The only way to change the wallpaper is to set it through the system settings on both Samsung and Pixel devices.

### Proposed fix
Investigate the wallpaper selection implementation and ensure that it properly updates the home screen wallpaper when a new wallpaper is selected. This may involve checking for necessary permissions, ensuring that the correct APIs are being called, and testing on multiple devices to confirm that the issue is resolved.

## 3 - Wallpaper overlay "blurring" effect does not work

### Description
The wallpaper overlay effect that is supposed to blur the wallpaper does not work as intended. The wallpaper remains clear and unblurred regardless of the selected overlay effect.

### Proposed fix
Investigate the implementation of the wallpaper overlay effect and ensure that the blurring effect is applied correctly. This may involve checking the rendering logic, verifying that the correct APIs are being used, and testing on multiple devices to confirm that the issue is resolved.

Note that this is a long-standing issue that has been present since the initial implementation of the wallpaper overlay effects. Consider simply removing this functionality if it is deemed too complex or time-consuming to fix, as it is not a critical feature and the other effects (darkening and color overlay) are working correctly.