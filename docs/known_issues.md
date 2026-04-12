# Known Issues

Status updated on 2026-04-12 based on user feedback and internal testing.

## 1 - Wallpaper overlay "blurring" effect does not work

Status: Fixed in code on 2026-04-12 for final release prep.

### Resolution
The unsupported blur wallpaper effect has been removed from the launcher and settings UI before the stable release. Existing installs and restored backups now normalize any saved `blur` preference back to the supported `darken` effect, and the settings activity now mirrors the remaining darken and color tint overlays.