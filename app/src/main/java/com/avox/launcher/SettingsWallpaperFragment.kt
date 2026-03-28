package com.avox.launcher

import android.app.WallpaperManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class SettingsWallpaperFragment : Fragment() {

    private val themeOptions = listOf(
        "light" to "Light",
        "dark" to "Dark",
        "system" to "Follow System"
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

    private val pickWallpaper = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val wm = WallpaperManager.getInstance(requireContext())
                requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                    wm.setStream(stream)
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        // Theme
        setupSpinner(
            view.findViewById(R.id.themeSpinner), themeOptions.map { it.second },
            themeOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_THEME, "system") ?: "system") }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_THEME, themeOptions[pos].first).apply()
            applyThemeMode(themeOptions[pos].first)
        }

        // Wallpaper effect controls
        val darkenControls = view.findViewById<View>(R.id.darkenControlsContainer)
        val blurControls = view.findViewById<View>(R.id.blurControlsContainer)
        val colorControls = view.findViewById<View>(R.id.colorControlsContainer)

        fun updateEffectControls(effect: String) {
            darkenControls.visibility = if (effect != MainActivity.WALLPAPER_EFFECT_BLUR) View.VISIBLE else View.GONE
            blurControls.visibility = if (effect == MainActivity.WALLPAPER_EFFECT_BLUR) View.VISIBLE else View.GONE
            colorControls.visibility = if (effect == MainActivity.WALLPAPER_EFFECT_COLOR) View.VISIBLE else View.GONE
        }

        val currentEffect = prefs.getString(MainActivity.PREF_WALLPAPER_EFFECT, MainActivity.WALLPAPER_EFFECT_DARKEN) ?: MainActivity.WALLPAPER_EFFECT_DARKEN
        updateEffectControls(currentEffect)

        setupSpinner(
            view.findViewById(R.id.wallpaperEffectSpinner), wallpaperEffectOptions.map { it.second },
            wallpaperEffectOptions.indexOfFirst { it.first == currentEffect }.coerceAtLeast(0)
        ) { pos ->
            val effect = wallpaperEffectOptions[pos].first
            prefs.edit().putString(MainActivity.PREF_WALLPAPER_EFFECT, effect).apply()
            updateEffectControls(effect)
        }

        // Darkness seekbar
        val darknessSeekBar = view.findViewById<SeekBar>(R.id.darknessSeekBar)
        val darknessValue = view.findViewById<TextView>(R.id.darknessValue)
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

        // Blur radius seekbar
        val blurRadiusSeekBar = view.findViewById<SeekBar>(R.id.blurRadiusSeekBar)
        val blurRadiusValue = view.findViewById<TextView>(R.id.blurRadiusValue)
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

        // Color tint spinner
        setupSpinner(
            view.findViewById(R.id.colorTintSpinner), colorTintOptions.map { it.second },
            colorTintOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_COLOR_TINT, MainActivity.DEFAULT_COLOR_TINT) ?: MainActivity.DEFAULT_COLOR_TINT) }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_COLOR_TINT, colorTintOptions[pos].first).apply()
        }

        // Wallpaper select
        view.findViewById<android.widget.Button>(R.id.wallpaperSelectButton).setOnClickListener {
            pickWallpaper.launch("image/*")
        }
    }

    private fun applyThemeMode(mode: String) {
        when (mode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, selection: Int, onSelected: (Int) -> Unit) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(selection)
        var initialized = false
        spinner.post {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (!initialized) { initialized = true; return }
                    onSelected(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}
