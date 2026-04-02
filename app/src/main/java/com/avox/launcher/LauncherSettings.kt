package com.avox.launcher

import android.content.Context
import android.content.SharedPreferences

object LauncherSettings {

    private val supportedLanguages = setOf(
        LauncherApp.LANGUAGE_TAG_SYSTEM,
        "da",
        "de",
        LauncherApp.LANGUAGE_TAG_ENGLISH,
        "es",
        "fi",
        "fr",
        "it",
        "nl",
        "nb",
        "pl",
        "pt",
        "sv"
    )

    private val supportedThemes = setOf("light", "dark", "system")
    private val supportedNotificationModes = setOf(
        MainActivity.NOTIF_MODE_COUNT,
        MainActivity.NOTIF_MODE_TEXT,
        MainActivity.NOTIF_MODE_NONE
    )
    private val supportedWallpaperEffects = setOf(
        MainActivity.WALLPAPER_EFFECT_DARKEN,
        MainActivity.WALLPAPER_EFFECT_BLUR,
        MainActivity.WALLPAPER_EFFECT_COLOR
    )
    private val supportedColorTints = setOf(
        MainActivity.DEFAULT_COLOR_TINT,
        "#1B5E20",
        "#B71C1C",
        "#4A148C",
        "#004D40",
        "#E65100"
    )
    private val supportedSpacingValues = setOf(4, 8, MainActivity.DEFAULT_SPACING, 14, 20)
    private val supportedAlignmentValues = setOf("left", "center")
    private val supportedIconModes = setOf(
        MainActivity.ICON_MODE_REGULAR,
        MainActivity.ICON_MODE_NERD,
        MainActivity.ICON_MODE_NONE
    )
    private val supportedFavoritesLayouts = setOf(
        MainActivity.FAVORITES_LAYOUT_ADAPTIVE,
        MainActivity.FAVORITES_LAYOUT_SINGLE
    )
    private val supportedFontValues = setOf(
        MainActivity.DEFAULT_FONT,
        MainActivity.CUSTOM_FONT_KEY
    )

    fun ensureInitialized(context: Context) {
        val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val mergedPreferences = defaultRestorablePreferences()

        prefs.all.forEach { (key, value) ->
            if (!isRestorablePreferenceKey(key) || isWidgetPreferenceKey(key) || value == null) {
                return@forEach
            }
            mergedPreferences[key] = value
        }

        val normalizedPreferences = normalizeRestorablePreferences(mergedPreferences)
        if (!needsInitialization(prefs, normalizedPreferences)) {
            return
        }

        applyRestorablePreferences(prefs, normalizedPreferences)
    }

    fun applyRestorablePreferences(
        prefs: SharedPreferences,
        rawPreferences: Map<String, Any>,
        widgetRestorePlanJson: String? = null
    ) {
        val normalizedPreferences = normalizeRestorablePreferences(rawPreferences)
        val editor = prefs.edit()
        val hasWidgetRestorePlan = !widgetRestorePlanJson.isNullOrBlank()
        val includesWidgetState = hasWidgetRestorePlan || normalizedPreferences.keys.any(::isWidgetPreferenceKey)

        prefs.all.keys
            .filter(::isRestorablePreferenceKey)
            .filter { includesWidgetState || !isWidgetPreferenceKey(it) }
            .forEach { editor.remove(it) }

        normalizedPreferences.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, LinkedHashSet(value.filterIsInstance<String>()))
            }
        }

        if (hasWidgetRestorePlan) {
            editor.putString(MainActivity.PREF_PENDING_WIDGET_RESTORE, widgetRestorePlanJson)
        } else if (includesWidgetState) {
            editor.remove(MainActivity.PREF_PENDING_WIDGET_RESTORE)
        }

        if (includesWidgetState) {
            editor.putBoolean(MainActivity.PREF_WIDGETS_DIRTY, true)
        }

        if (!editor.commit()) {
            throw IllegalStateException("Failed to commit launcher settings")
        }
    }

    fun isWidgetPreferenceKey(key: String): Boolean {
        return key == MainActivity.PREF_WIDGET_ORDER ||
            key.startsWith("widget_h_") ||
            key.startsWith("widget_fw_")
    }

    fun isRestorablePreferenceKey(key: String): Boolean {
        return key != MainActivity.PREF_WIDGET_IDS_OLD &&
            key != MainActivity.PREF_PENDING_WIDGET_RESTORE &&
            key != MainActivity.PREF_WIDGETS_DIRTY
    }

    internal fun defaultRestorablePreferences(): LinkedHashMap<String, Any> {
        return linkedMapOf(
            MainActivity.PREF_FONT to MainActivity.DEFAULT_FONT,
            MainActivity.PREF_SPACING to MainActivity.DEFAULT_SPACING,
            MainActivity.PREF_DARKNESS to MainActivity.DEFAULT_DARKNESS,
            MainActivity.PREF_FONT_SIZE to MainActivity.DEFAULT_FONT_SIZE,
            MainActivity.PREF_SIDEBAR_FONT_SIZE to MainActivity.DEFAULT_SIDEBAR_FONT_SIZE,
            MainActivity.PREF_THEME to "system",
            MainActivity.PREF_LANGUAGE to LauncherApp.LANGUAGE_TAG_SYSTEM,
            MainActivity.PREF_NOTIF_MODE to MainActivity.NOTIF_MODE_COUNT,
            MainActivity.PREF_SHOW_WIDGET_SLOT_INDICATOR to true,
            MainActivity.PREF_ANIM_STYLE to AlphabetSidebar.STYLE_WAVE,
            MainActivity.PREF_WAVE_SHIFT to MainActivity.DEFAULT_WAVE_SHIFT,
            MainActivity.PREF_WAVE_SCALE to MainActivity.DEFAULT_WAVE_SCALE,
            MainActivity.PREF_WAVE_RADIUS to MainActivity.DEFAULT_WAVE_RADIUS,
            MainActivity.PREF_HIGHLIGHT_INTENSITY to MainActivity.DEFAULT_HIGHLIGHT_INTENSITY,
            MainActivity.PREF_FADE_RADIUS to MainActivity.DEFAULT_FADE_RADIUS,
            MainActivity.PREF_ALIGNMENT to "left",
            MainActivity.PREF_H_MARGIN to MainActivity.DEFAULT_H_MARGIN,
            MainActivity.PREF_V_MARGIN to MainActivity.DEFAULT_V_MARGIN,
            MainActivity.PREF_BLOCK_COUNT to MainActivity.DEFAULT_BLOCK_COUNT,
            MainActivity.PREF_FAVORITES to MainActivity.DEFAULT_FAVORITES.joinToString(","),
            MainActivity.PREF_FAVORITES_LAYOUT to MainActivity.DEFAULT_FAVORITES_LAYOUT,
            MainActivity.PREF_WALLPAPER_EFFECT to MainActivity.WALLPAPER_EFFECT_DARKEN,
            MainActivity.PREF_BLUR_RADIUS to MainActivity.DEFAULT_BLUR_RADIUS,
            MainActivity.PREF_COLOR_TINT to MainActivity.DEFAULT_COLOR_TINT,
            MainActivity.PREF_NOTIF_SWIPE to false,
            MainActivity.PREF_ICON_SIZE to MainActivity.DEFAULT_ICON_SIZE,
            MainActivity.PREF_ICON_PACK to "",
            MainActivity.PREF_NERD_FONT to false,
            MainActivity.PREF_HIDE_STATUS_BAR to false,
            MainActivity.PREF_FOOTER_NOTIF_MODE to MainActivity.NOTIF_MODE_NONE,
            MainActivity.PREF_FOOTER_SHOW_LABELS to false,
            MainActivity.PREF_QUICK_ACTIONS_BOTTOM_OFFSET to MainActivity.DEFAULT_QUICK_ACTIONS_BOTTOM_OFFSET,
            MainActivity.PREF_ICON_MODE to MainActivity.ICON_MODE_REGULAR
        )
    }

    internal fun normalizeRestorablePreferences(rawPreferences: Map<String, Any>): LinkedHashMap<String, Any> {
        val normalizedPreferences = linkedMapOf<String, Any>()
        rawPreferences.forEach { (key, value) ->
            if (!isRestorablePreferenceKey(key)) {
                return@forEach
            }
            normalizePreference(key, value)?.let { normalizedPreferences[key] = it }
        }
        return normalizedPreferences
    }

    private fun needsInitialization(
        prefs: SharedPreferences,
        normalizedPreferences: LinkedHashMap<String, Any>
    ): Boolean {
        normalizedPreferences.forEach { (key, expectedValue) ->
            val currentValue = prefs.all[key] ?: return true
            val normalizedCurrent = normalizePreference(key, currentValue) ?: return true
            if (!preferencesEqual(normalizedCurrent, expectedValue)) {
                return true
            }
        }
        return false
    }

    private fun preferencesEqual(left: Any, right: Any): Boolean {
        return if (left is Set<*> && right is Set<*>) {
            left == right
        } else {
            left == right
        }
    }

    private fun normalizePreference(key: String, value: Any): Any? {
        return when {
            key == MainActivity.PREF_FONT -> normalizeStringOption(value, supportedFontValues, MainActivity.DEFAULT_FONT)
            key == MainActivity.PREF_SPACING -> normalizeIntOption(value, supportedSpacingValues, MainActivity.DEFAULT_SPACING)
            key == MainActivity.PREF_DARKNESS -> normalizeIntRange(value, 0, 100, MainActivity.DEFAULT_DARKNESS)
            key == MainActivity.PREF_FONT_SIZE -> normalizeIntRange(value, 12, 36, MainActivity.DEFAULT_FONT_SIZE)
            key == MainActivity.PREF_SIDEBAR_FONT_SIZE -> normalizeIntRange(value, 8, 32, MainActivity.DEFAULT_SIDEBAR_FONT_SIZE)
            key == MainActivity.PREF_THEME -> normalizeStringOption(value, supportedThemes, "system")
            key == MainActivity.PREF_LANGUAGE -> normalizeLanguage(value)
            key == MainActivity.PREF_NOTIF_MODE -> normalizeStringOption(value, supportedNotificationModes, MainActivity.NOTIF_MODE_COUNT)
            key == MainActivity.PREF_WIDGET_ORDER -> value as? String ?: ""
            key == MainActivity.PREF_SHOW_WIDGET_SLOT_INDICATOR -> value as? Boolean ?: true
            key == MainActivity.PREF_ANIM_STYLE -> normalizeStringOption(
                value,
                setOf(AlphabetSidebar.STYLE_WAVE, AlphabetSidebar.STYLE_HIGHLIGHT, AlphabetSidebar.STYLE_FADE),
                AlphabetSidebar.STYLE_WAVE
            )
            key == MainActivity.PREF_WAVE_SHIFT -> normalizeIntRange(value, 0, 400, MainActivity.DEFAULT_WAVE_SHIFT)
            key == MainActivity.PREF_WAVE_SCALE -> normalizeIntRange(value, 0, 20, MainActivity.DEFAULT_WAVE_SCALE)
            key == MainActivity.PREF_WAVE_RADIUS -> normalizeIntRange(value, 0, 30, MainActivity.DEFAULT_WAVE_RADIUS)
            key == MainActivity.PREF_HIGHLIGHT_INTENSITY -> normalizeIntRange(value, 0, 20, MainActivity.DEFAULT_HIGHLIGHT_INTENSITY)
            key == MainActivity.PREF_FADE_RADIUS -> normalizeIntRange(value, 0, 20, MainActivity.DEFAULT_FADE_RADIUS)
            key == MainActivity.PREF_ALIGNMENT -> normalizeStringOption(value, supportedAlignmentValues, "left")
            key == MainActivity.PREF_H_MARGIN -> normalizeIntRange(value, 0, 120, MainActivity.DEFAULT_H_MARGIN)
            key == MainActivity.PREF_V_MARGIN -> normalizeIntRange(value, 0, 120, MainActivity.DEFAULT_V_MARGIN)
            key == MainActivity.PREF_BLOCK_COUNT -> normalizeIntOption(value, setOf(2, 3, 4), MainActivity.DEFAULT_BLOCK_COUNT)
            key == MainActivity.PREF_FAVORITES -> normalizeFavorites(value)
            key == MainActivity.PREF_FAVORITES_LAYOUT -> normalizeStringOption(value, supportedFavoritesLayouts, MainActivity.DEFAULT_FAVORITES_LAYOUT)
            key == MainActivity.PREF_WALLPAPER_EFFECT -> normalizeStringOption(value, supportedWallpaperEffects, MainActivity.WALLPAPER_EFFECT_DARKEN)
            key == MainActivity.PREF_BLUR_RADIUS -> normalizeIntRange(value, 0, 25, MainActivity.DEFAULT_BLUR_RADIUS)
            key == MainActivity.PREF_COLOR_TINT -> normalizeStringOption(value, supportedColorTints, MainActivity.DEFAULT_COLOR_TINT)
            key == MainActivity.PREF_NOTIF_SWIPE -> value as? Boolean ?: false
            key == MainActivity.PREF_ICON_SIZE -> normalizeIntRange(value, 16, 64, MainActivity.DEFAULT_ICON_SIZE)
            key == MainActivity.PREF_ICON_PACK -> value as? String ?: ""
            key == MainActivity.PREF_NERD_FONT -> value as? Boolean ?: false
            key == MainActivity.PREF_HIDE_STATUS_BAR -> value as? Boolean ?: false
            key == MainActivity.PREF_FOOTER_NOTIF_MODE -> normalizeStringOption(value, supportedNotificationModes, MainActivity.NOTIF_MODE_NONE)
            key == MainActivity.PREF_FOOTER_SHOW_LABELS -> value as? Boolean ?: false
            key == MainActivity.PREF_QUICK_ACTIONS_BOTTOM_OFFSET -> normalizeIntRange(value, 0, 120, MainActivity.DEFAULT_QUICK_ACTIONS_BOTTOM_OFFSET)
            key == MainActivity.PREF_ICON_MODE -> normalizeStringOption(value, supportedIconModes, MainActivity.ICON_MODE_REGULAR)
            key.startsWith("widget_h_") -> normalizePositiveInt(value)
            key.startsWith("widget_fw_") -> value as? Boolean ?: false
            key.startsWith("footer_action_") -> value as? String ?: ""
            else -> normalizeGenericValue(value)
        }
    }

    private fun normalizeLanguage(value: Any): String {
        val language = (value as? String)?.trim() ?: LauncherApp.LANGUAGE_TAG_SYSTEM
        return if (language.isEmpty() || language in supportedLanguages) {
            language
        } else {
            LauncherApp.LANGUAGE_TAG_SYSTEM
        }
    }

    private fun normalizeFavorites(value: Any): String {
        val favorites = (value as? String).orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        return if (favorites.isEmpty()) {
            MainActivity.DEFAULT_FAVORITES.joinToString(",")
        } else {
            favorites.joinToString(",")
        }
    }

    private fun normalizeStringOption(value: Any, allowedValues: Set<String>, defaultValue: String): String {
        val stringValue = (value as? String)?.trim().orEmpty()
        return if (stringValue in allowedValues) stringValue else defaultValue
    }

    private fun normalizeIntOption(value: Any, allowedValues: Set<Int>, defaultValue: Int): Int {
        val intValue = (value as? Number)?.toInt() ?: return defaultValue
        return if (intValue in allowedValues) intValue else defaultValue
    }

    private fun normalizeIntRange(value: Any, min: Int, max: Int, defaultValue: Int): Int {
        val intValue = (value as? Number)?.toInt() ?: return defaultValue
        return intValue.coerceIn(min, max)
    }

    private fun normalizePositiveInt(value: Any): Int? {
        val intValue = (value as? Number)?.toInt() ?: return null
        return intValue.takeIf { it > 0 }
    }

    private fun normalizeGenericValue(value: Any): Any? {
        return when (value) {
            is Boolean,
            is Int,
            is Long,
            is Float,
            is String -> value

            is Set<*> -> LinkedHashSet(value.filterIsInstance<String>())

            else -> null
        }
    }
}