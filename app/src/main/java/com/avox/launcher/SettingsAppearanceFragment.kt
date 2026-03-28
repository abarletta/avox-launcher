package com.avox.launcher

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
import androidx.fragment.app.Fragment
import java.io.File

class SettingsAppearanceFragment : Fragment() {

    private val pickFont = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val dest = File(requireContext().filesDir, "custom_font.ttf")
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                (requireActivity().application as? LauncherApp)?.loadCustomFont()
                val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(MainActivity.PREF_FONT, MainActivity.CUSTOM_FONT_KEY).apply()
            } catch (_: Exception) { }
        }
    }

    private val pickNerdFont = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val dest = File(requireContext().filesDir, "nerd_font.ttf")
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings_appearance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val fontOptions = listOf(
            "sans-serif" to getString(R.string.option_default),
            "sans-serif-light" to getString(R.string.option_light),
            "sans-serif-thin" to getString(R.string.option_thin),
            "sans-serif-condensed" to getString(R.string.option_condensed),
            "serif" to getString(R.string.option_serif),
            "monospace" to getString(R.string.option_monospace),
            MainActivity.CUSTOM_FONT_KEY to getString(R.string.option_custom_ttf)
        )
        val spacingOptions = listOf(
            4 to getString(R.string.option_compact),
            8 to getString(R.string.option_normal),
            14 to getString(R.string.option_spacious),
            20 to getString(R.string.option_large)
        )
        val alignmentOptions = listOf(
            "left" to getString(R.string.option_left),
            "center" to getString(R.string.option_center)
        )
        val iconModeOptions = listOf(
            MainActivity.ICON_MODE_REGULAR to getString(R.string.icon_mode_regular),
            MainActivity.ICON_MODE_NERD to getString(R.string.icon_mode_nerd),
            MainActivity.ICON_MODE_NONE to getString(R.string.icon_mode_none)
        )

        // Font
        setupSpinner(
            view.findViewById(R.id.fontSpinner), fontOptions.map { it.second },
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
        val fontSizeSeekBar = view.findViewById<SeekBar>(R.id.fontSizeSeekBar)
        val fontSizeValue = view.findViewById<TextView>(R.id.fontSizeValue)
        val currentFontSize = prefs.getInt(MainActivity.PREF_FONT_SIZE, MainActivity.DEFAULT_FONT_SIZE)
        fontSizeSeekBar.progress = currentFontSize - 12
        fontSizeValue.text = getString(R.string.settings_value_sp, currentFontSize)
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = progress + 12
                fontSizeValue.text = getString(R.string.settings_value_sp, size)
                prefs.edit().putInt(MainActivity.PREF_FONT_SIZE, size).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Spacing
        setupSpinner(
            view.findViewById(R.id.spacingSpinner), spacingOptions.map { it.second },
            spacingOptions.indexOfFirst { it.first == prefs.getInt(MainActivity.PREF_SPACING, MainActivity.DEFAULT_SPACING) }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putInt(MainActivity.PREF_SPACING, spacingOptions[pos].first).apply()
        }

        // Alignment
        setupSpinner(
            view.findViewById(R.id.alignmentSpinner), alignmentOptions.map { it.second },
            alignmentOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_ALIGNMENT, "left") ?: "left") }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_ALIGNMENT, alignmentOptions[pos].first).apply()
        }

        // Horizontal margin
        val hMarginSeekBar = view.findViewById<SeekBar>(R.id.hMarginSeekBar)
        val hMarginValue = view.findViewById<TextView>(R.id.hMarginValue)
        val currentHM = prefs.getInt(MainActivity.PREF_H_MARGIN, MainActivity.DEFAULT_H_MARGIN)
        hMarginSeekBar.progress = currentHM
        hMarginValue.text = getString(R.string.settings_value_dp, currentHM)
        hMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                hMarginValue.text = getString(R.string.settings_value_dp, progress)
                prefs.edit().putInt(MainActivity.PREF_H_MARGIN, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Vertical margin
        val vMarginSeekBar = view.findViewById<SeekBar>(R.id.vMarginSeekBar)
        val vMarginValue = view.findViewById<TextView>(R.id.vMarginValue)
        val currentVM = prefs.getInt(MainActivity.PREF_V_MARGIN, MainActivity.DEFAULT_V_MARGIN)
        vMarginSeekBar.progress = currentVM
        vMarginValue.text = getString(R.string.settings_value_dp, currentVM)
        vMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                vMarginValue.text = getString(R.string.settings_value_dp, progress)
                prefs.edit().putInt(MainActivity.PREF_V_MARGIN, progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Icon mode
        val currentIconMode = prefs.getString(MainActivity.PREF_ICON_MODE, null)
            ?: if (prefs.getBoolean(MainActivity.PREF_NERD_FONT, false)) MainActivity.ICON_MODE_NERD else MainActivity.ICON_MODE_REGULAR
        setupSpinner(
            view.findViewById(R.id.iconModeSpinner), iconModeOptions.map { it.second },
            iconModeOptions.indexOfFirst { it.first == currentIconMode }.coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_ICON_MODE, iconModeOptions[pos].first).apply()
        }

        // Icon size
        val iconSizeSeekBar = view.findViewById<SeekBar>(R.id.iconSizeSeekBar)
        val iconSizeValue = view.findViewById<TextView>(R.id.iconSizeValue)
        val currentIconSize = prefs.getInt(MainActivity.PREF_ICON_SIZE, MainActivity.DEFAULT_ICON_SIZE)
        iconSizeSeekBar.progress = currentIconSize - 16
        iconSizeValue.text = getString(R.string.settings_value_dp, currentIconSize)
        iconSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = progress + 16
                iconSizeValue.text = getString(R.string.settings_value_dp, size)
                prefs.edit().putInt(MainActivity.PREF_ICON_SIZE, size).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Icon pack
        val installedPacks = IconPackResolver.getInstalledPacks(requireContext())
    val iconPackOptions = mutableListOf("" to getString(R.string.option_default))
        iconPackOptions.addAll(installedPacks)
        val currentPack = prefs.getString(MainActivity.PREF_ICON_PACK, "") ?: ""
        setupSpinner(
            view.findViewById(R.id.iconPackSpinner), iconPackOptions.map { it.second },
            iconPackOptions.indexOfFirst { it.first == currentPack }.coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putString(MainActivity.PREF_ICON_PACK, iconPackOptions[pos].first).apply()
        }

        // Nerd font picker
        view.findViewById<android.widget.Button>(R.id.nerdFontPickButton).setOnClickListener {
            pickNerdFont.launch("font/*")
        }

        // Hide status bar
        val hideStatusBarSwitch = view.findViewById<android.widget.Switch>(R.id.hideStatusBarSwitch)
        hideStatusBarSwitch.isChecked = prefs.getBoolean(MainActivity.PREF_HIDE_STATUS_BAR, false)
        hideStatusBarSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(MainActivity.PREF_HIDE_STATUS_BAR, checked).apply()
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
