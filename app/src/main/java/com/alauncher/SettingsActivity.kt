package com.alauncher

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private val fontOptions = listOf(
        "sans-serif" to "Default",
        "sans-serif-light" to "Light",
        "sans-serif-thin" to "Thin",
        "sans-serif-condensed" to "Condensed",
        "serif" to "Serif",
        "monospace" to "Monospace",
        MainActivity.CUSTOM_FONT_KEY to "Custom TTF"
    )

    private val spacingOptions = listOf(
        4 to "Compact",
        8 to "Normal",
        14 to "Spacious",
        20 to "Large"
    )

    private val themeOptions = listOf(
        "light" to "Light",
        "dark" to "Dark",
        "system" to "Follow System"
    )

    private val notifOptions = listOf(
        MainActivity.NOTIF_MODE_COUNT to "Badge Count",
        MainActivity.NOTIF_MODE_TEXT to "Notification Text",
        MainActivity.NOTIF_MODE_NONE to "Off"
    )

    private val animStyleOptions = listOf(
        AlphabetSidebar.STYLE_WAVE to "Wave / Zoom",
        AlphabetSidebar.STYLE_HIGHLIGHT to "Highlight",
        AlphabetSidebar.STYLE_FADE to "Fade"
    )

    private val alignmentOptions = listOf(
        "left" to "Left",
        "center" to "Center"
    )

    private val blockCountOptions = listOf(
        2 to "2 (default)",
        3 to "3",
        4 to "4"
    )

    private val wallpaperEffectOptions = listOf(
        MainActivity.WALLPAPER_EFFECT_DARKEN to "Darken",
        MainActivity.WALLPAPER_EFFECT_BLUR to "Blur",
        MainActivity.WALLPAPER_EFFECT_COLOR to "Color Tint"
    )

    private val colorTintOptions = listOf(
        "#1A237E" to "Indigo",
        "#1B5E20" to "Green",
        "#B71C1C" to "Red",
        "#4A148C" to "Purple",
        "#004D40" to "Teal",
        "#E65100" to "Orange"
    )

    private val pickFont = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val dest = File(filesDir, "custom_font.ttf")
                contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                (application as? LauncherApp)?.loadCustomFont()
                val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                prefs.edit().putString(MainActivity.PREF_FONT, MainActivity.CUSTOM_FONT_KEY).apply()
            } catch (_: Exception) { }
        }
    }

    private val pickNerdFont = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val dest = java.io.File(filesDir, "nerd_font.ttf")
                contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            } catch (_: Exception) { }
        }
    }

    private val pickWallpaper = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val wm = WallpaperManager.getInstance(this)
                contentResolver.openInputStream(uri)?.use { stream ->
                    wm.setStream(stream)
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPrefs()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)

        // Theme
        setupSpinner(
            R.id.themeSpinner, themeOptions.map { it.second },
            themeOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_THEME, "system") ?: "system") }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_THEME, themeOptions[pos].first).apply()
            applyThemeMode(themeOptions[pos].first)
        }

        // Settings overlay
        val settingsOverlay = findViewById<View>(R.id.settingsOverlay)
        settingsOverlay.alpha = prefs.getInt(MainActivity.PREF_DARKNESS, MainActivity.DEFAULT_DARKNESS) / 100f

        // Wallpaper effect controls
        val darkenControls = findViewById<View>(R.id.darkenControlsContainer)
        val blurControls = findViewById<View>(R.id.blurControlsContainer)
        val colorControls = findViewById<View>(R.id.colorControlsContainer)

        fun updateEffectControls(effect: String) {
            darkenControls.visibility = if (effect != MainActivity.WALLPAPER_EFFECT_BLUR) View.VISIBLE else View.GONE
            blurControls.visibility = if (effect == MainActivity.WALLPAPER_EFFECT_BLUR) View.VISIBLE else View.GONE
            colorControls.visibility = if (effect == MainActivity.WALLPAPER_EFFECT_COLOR) View.VISIBLE else View.GONE
        }

        val currentEffect = prefs.getString(MainActivity.PREF_WALLPAPER_EFFECT, MainActivity.WALLPAPER_EFFECT_DARKEN) ?: MainActivity.WALLPAPER_EFFECT_DARKEN
        updateEffectControls(currentEffect)

        setupSpinner(
            R.id.wallpaperEffectSpinner, wallpaperEffectOptions.map { it.second },
            wallpaperEffectOptions.indexOfFirst { it.first == currentEffect }
                .coerceAtLeast(0)
        ) { pos ->
            val effect = wallpaperEffectOptions[pos].first
            prefs.edit().putString(MainActivity.PREF_WALLPAPER_EFFECT, effect).apply()
            updateEffectControls(effect)
        }

        // Wallpaper darkness
        val darknessSeekBar = findViewById<SeekBar>(R.id.darknessSeekBar)
        val darknessValue = findViewById<TextView>(R.id.darknessValue)
        val currentDarkness = prefs.getInt(MainActivity.PREF_DARKNESS, MainActivity.DEFAULT_DARKNESS)
        darknessSeekBar.progress = currentDarkness
        darknessValue.text = "$currentDarkness%"
        darknessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                darknessValue.text = "$progress%"
                prefs.edit().putInt(MainActivity.PREF_DARKNESS, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Blur radius
        val blurRadiusSeekBar = findViewById<SeekBar>(R.id.blurRadiusSeekBar)
        val blurRadiusValue = findViewById<TextView>(R.id.blurRadiusValue)
        val currentBlur = prefs.getInt(MainActivity.PREF_BLUR_RADIUS, MainActivity.DEFAULT_BLUR_RADIUS)
        blurRadiusSeekBar.progress = currentBlur
        blurRadiusValue.text = "$currentBlur"
        blurRadiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blurRadiusValue.text = "$progress"
                prefs.edit().putInt(MainActivity.PREF_BLUR_RADIUS, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Color tint
        setupSpinner(
            R.id.colorTintSpinner, colorTintOptions.map { it.second },
            colorTintOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_COLOR_TINT, MainActivity.DEFAULT_COLOR_TINT) ?: MainActivity.DEFAULT_COLOR_TINT) }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_COLOR_TINT, colorTintOptions[pos].first).apply()
        }

        // Font
        setupSpinner(
            R.id.fontSpinner, fontOptions.map { it.second },
            fontOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_FONT, MainActivity.DEFAULT_FONT) ?: MainActivity.DEFAULT_FONT) }
                .coerceAtLeast(0)
        ) { pos ->
            val key = fontOptions[pos].first
            if (key == MainActivity.CUSTOM_FONT_KEY) {
                pickFont.launch("font/*")
            } else {
                prefs.edit().putString(MainActivity.PREF_FONT, key).apply()
            }
        }

        // Font size
        val fontSizeSeekBar = findViewById<SeekBar>(R.id.fontSizeSeekBar)
        val fontSizeValue = findViewById<TextView>(R.id.fontSizeValue)
        val currentFontSize = prefs.getInt(MainActivity.PREF_FONT_SIZE, MainActivity.DEFAULT_FONT_SIZE)
        fontSizeSeekBar.progress = currentFontSize - 12
        fontSizeValue.text = "${currentFontSize}sp"
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = progress + 12
                fontSizeValue.text = "${size}sp"
                prefs.edit().putInt(MainActivity.PREF_FONT_SIZE, size).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Icon size
        val iconSizeSeekBar = findViewById<SeekBar>(R.id.iconSizeSeekBar)
        val iconSizeValue = findViewById<TextView>(R.id.iconSizeValue)
        val currentIconSize = prefs.getInt(MainActivity.PREF_ICON_SIZE, MainActivity.DEFAULT_ICON_SIZE)
        iconSizeSeekBar.progress = currentIconSize - 16  // min 16dp
        iconSizeValue.text = "${currentIconSize}dp"
        iconSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = progress + 16
                iconSizeValue.text = "${size}dp"
                prefs.edit().putInt(MainActivity.PREF_ICON_SIZE, size).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Icon pack
        val installedPacks = IconPackResolver.getInstalledPacks(this)
        val iconPackOptions = mutableListOf("" to "Default")
        iconPackOptions.addAll(installedPacks)
        val currentPack = prefs.getString(MainActivity.PREF_ICON_PACK, "") ?: ""
        setupSpinner(
            R.id.iconPackSpinner, iconPackOptions.map { it.second },
            iconPackOptions.indexOfFirst { it.first == currentPack }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_ICON_PACK, iconPackOptions[pos].first).apply()
        }

        // Nerd font
        @Suppress("UseSwitchCompatOrMaterialCode")
        val nerdSwitch = findViewById<android.widget.Switch>(R.id.nerdFontSwitch)
        nerdSwitch.isChecked = prefs.getBoolean(MainActivity.PREF_NERD_FONT, false)
        nerdSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(MainActivity.PREF_NERD_FONT, isChecked).apply()
        }
        findViewById<android.widget.Button>(R.id.nerdFontPickButton).setOnClickListener {
            pickNerdFont.launch("font/*")
        }

        // Spacing
        setupSpinner(
            R.id.spacingSpinner, spacingOptions.map { it.second },
            spacingOptions.indexOfFirst { it.first == prefs.getInt(MainActivity.PREF_SPACING, MainActivity.DEFAULT_SPACING) }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putInt(MainActivity.PREF_SPACING, spacingOptions[pos].first).apply()
        }

        // Notification mode
        setupSpinner(
            R.id.notifModeSpinner, notifOptions.map { it.second },
            notifOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_NOTIF_MODE, MainActivity.NOTIF_MODE_COUNT) ?: MainActivity.NOTIF_MODE_COUNT) }
                .coerceAtLeast(0)
        ) { pos ->
            val mode = notifOptions[pos].first
            prefs.edit().putString(MainActivity.PREF_NOTIF_MODE, mode).apply()
            if (mode != MainActivity.NOTIF_MODE_NONE && !isNotificationListenerEnabled()) {
                promptNotificationAccess()
            }
        }

        // Notification swipe
        @Suppress("UseSwitchCompatOrMaterialCode")
        val swipeSwitch = findViewById<android.widget.Switch>(R.id.notifSwipeSwitch)
        swipeSwitch.isChecked = prefs.getBoolean(MainActivity.PREF_NOTIF_SWIPE, false)
        swipeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(MainActivity.PREF_NOTIF_SWIPE, isChecked).apply()
        }

        // Wallpaper select
        findViewById<Button>(R.id.wallpaperSelectButton).setOnClickListener {
            pickWallpaper.launch("image/*")
        }

        // Animation style with conditional controls
        val waveControls = findViewById<View>(R.id.waveControlsContainer)
        val highlightControls = findViewById<View>(R.id.highlightControlsContainer)
        val fadeControls = findViewById<View>(R.id.fadeControlsContainer)

        fun updateAnimControls(style: String) {
            waveControls.visibility = if (style == AlphabetSidebar.STYLE_WAVE) View.VISIBLE else View.GONE
            highlightControls.visibility = if (style == AlphabetSidebar.STYLE_HIGHLIGHT) View.VISIBLE else View.GONE
            fadeControls.visibility = if (style == AlphabetSidebar.STYLE_FADE) View.VISIBLE else View.GONE
        }

        val currentAnimStyle = prefs.getString(MainActivity.PREF_ANIM_STYLE, AlphabetSidebar.STYLE_WAVE) ?: AlphabetSidebar.STYLE_WAVE
        updateAnimControls(currentAnimStyle)

        setupSpinner(
            R.id.animStyleSpinner, animStyleOptions.map { it.second },
            animStyleOptions.indexOfFirst { it.first == currentAnimStyle }
                .coerceAtLeast(0)
        ) { pos ->
            val style = animStyleOptions[pos].first
            prefs.edit().putString(MainActivity.PREF_ANIM_STYLE, style).apply()
            updateAnimControls(style)
        }

        // Wave shift
        val waveShiftSeekBar = findViewById<SeekBar>(R.id.waveShiftSeekBar)
        val waveShiftValue = findViewById<TextView>(R.id.waveShiftValue)
        val currentShift = prefs.getInt(MainActivity.PREF_WAVE_SHIFT, MainActivity.DEFAULT_WAVE_SHIFT)
        waveShiftSeekBar.progress = currentShift
        waveShiftValue.text = "$currentShift"
        waveShiftSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                waveShiftValue.text = "$progress"
                prefs.edit().putInt(MainActivity.PREF_WAVE_SHIFT, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Wave scale
        val waveScaleSeekBar = findViewById<SeekBar>(R.id.waveScaleSeekBar)
        val waveScaleValue = findViewById<TextView>(R.id.waveScaleValue)
        val currentScale = prefs.getInt(MainActivity.PREF_WAVE_SCALE, MainActivity.DEFAULT_WAVE_SCALE)
        waveScaleSeekBar.progress = currentScale
        waveScaleValue.text = "${1f + currentScale / 10f}x"
        waveScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                waveScaleValue.text = "${1f + progress / 10f}x"
                prefs.edit().putInt(MainActivity.PREF_WAVE_SCALE, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Highlight intensity
        val highlightSeekBar = findViewById<SeekBar>(R.id.highlightIntensitySeekBar)
        val highlightValue = findViewById<TextView>(R.id.highlightIntensityValue)
        val currentHighlight = prefs.getInt(MainActivity.PREF_HIGHLIGHT_INTENSITY, MainActivity.DEFAULT_HIGHLIGHT_INTENSITY)
        highlightSeekBar.progress = currentHighlight
        highlightValue.text = "${currentHighlight / 10f}x"
        highlightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                highlightValue.text = "${progress / 10f}x"
                prefs.edit().putInt(MainActivity.PREF_HIGHLIGHT_INTENSITY, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Fade radius
        val fadeSeekBar = findViewById<SeekBar>(R.id.fadeRadiusSeekBar)
        val fadeValue = findViewById<TextView>(R.id.fadeRadiusValue)
        val currentFade = prefs.getInt(MainActivity.PREF_FADE_RADIUS, MainActivity.DEFAULT_FADE_RADIUS)
        fadeSeekBar.progress = currentFade
        fadeValue.text = "${currentFade / 10f}x"
        fadeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                fadeValue.text = "${progress / 10f}x"
                prefs.edit().putInt(MainActivity.PREF_FADE_RADIUS, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Alignment
        setupSpinner(
            R.id.alignmentSpinner, alignmentOptions.map { it.second },
            alignmentOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_ALIGNMENT, "left") ?: "left") }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_ALIGNMENT, alignmentOptions[pos].first).apply()
        }

        // Horizontal margin
        val hMarginSeekBar = findViewById<SeekBar>(R.id.hMarginSeekBar)
        val hMarginValue = findViewById<TextView>(R.id.hMarginValue)
        val currentHM = prefs.getInt(MainActivity.PREF_H_MARGIN, MainActivity.DEFAULT_H_MARGIN)
        hMarginSeekBar.progress = currentHM
        hMarginValue.text = "${currentHM}dp"
        hMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hMarginValue.text = "${progress}dp"
                prefs.edit().putInt(MainActivity.PREF_H_MARGIN, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Vertical margin
        val vMarginSeekBar = findViewById<SeekBar>(R.id.vMarginSeekBar)
        val vMarginValue = findViewById<TextView>(R.id.vMarginValue)
        val currentVM = prefs.getInt(MainActivity.PREF_V_MARGIN, MainActivity.DEFAULT_V_MARGIN)
        vMarginSeekBar.progress = currentVM
        vMarginValue.text = "${currentVM}dp"
        vMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                vMarginValue.text = "${progress}dp"
                prefs.edit().putInt(MainActivity.PREF_V_MARGIN, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Block count
        setupSpinner(
            R.id.blockCountSpinner, blockCountOptions.map { it.second },
            blockCountOptions.indexOfFirst { it.first == prefs.getInt(MainActivity.PREF_BLOCK_COUNT, MainActivity.DEFAULT_BLOCK_COUNT) }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putInt(MainActivity.PREF_BLOCK_COUNT, blockCountOptions[pos].first).apply()
        }

        // Favorites picker
        findViewById<Button>(R.id.favoritesButton).setOnClickListener {
            showFavoritesPicker()
        }

        // Add Widget button
        findViewById<Button>(R.id.addWidgetButton).setOnClickListener {
            finish()
            // Signal to main activity to open widget picker
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("open_widget_picker", true)
            }
            startActivity(intent)
        }
    }

    private fun showFavoritesPicker() {
        val pm = packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val apps = pm.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { ri ->
                val ai = ri.activityInfo ?: return@mapNotNull null
                ai.packageName to (ri.loadLabel(pm)?.toString() ?: ai.packageName)
            }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val currentFavs = (prefs.getString(MainActivity.PREF_FAVORITES, null)
            ?.split(",")?.filter { it.isNotBlank() }?.toSet()
            ?: MainActivity.DEFAULT_FAVORITES)

        val labels = apps.map { it.second }.toTypedArray()
        val checked = BooleanArray(apps.size) { currentFavs.contains(apps[it].first) }

        AlertDialog.Builder(this)
            .setTitle(R.string.favorites_select_label)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(R.string.grant) { _, _ ->
                val selected = apps.filterIndexed { i, _ -> checked[i] }.map { it.first }
                if (selected.size < 3) {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.favorites_min_max)
                        .setPositiveButton(R.string.grant, null)
                        .show()
                    return@setPositiveButton
                }
                val capped = selected.take(10)
                prefs.edit().putString(MainActivity.PREF_FAVORITES, capped.joinToString(",")).apply()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupSpinner(viewId: Int, items: List<String>, selection: Int, onSelected: (Int) -> Unit) {
        val spinner = findViewById<Spinner>(viewId)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(selection)
        var initialized = false
        spinner.post {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (!initialized) {
                        initialized = true
                        return
                    }
                    onSelected(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun applyThemeFromPrefs() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val theme = prefs.getString(MainActivity.PREF_THEME, "system") ?: "system"
        applyThemeMode(theme)
    }

    private fun applyThemeMode(mode: String) {
        when (mode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        val component = ComponentName(this, NotificationService::class.java)
        return flat.contains(component.flattenToString())
    }

    private fun promptNotificationAccess() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notification_access_title)
            .setMessage(R.string.notification_access_message)
            .setPositiveButton(R.string.grant) { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
