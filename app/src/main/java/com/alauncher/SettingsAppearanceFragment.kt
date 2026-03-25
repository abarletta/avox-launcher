package com.alauncher

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

    private val alignmentOptions = listOf(
        "left" to "Left",
        "center" to "Center"
    )

    private val blockCountOptions = listOf(
        2 to "2 (default)",
        3 to "3",
        4 to "4"
    )

    private val iconModeOptions = listOf(
        MainActivity.ICON_MODE_REGULAR to "Regular Icons",
        MainActivity.ICON_MODE_NERD to "Nerd Font Icons",
        MainActivity.ICON_MODE_NONE to "No Icons"
    )

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
        val vMarginSeekBar = view.findViewById<SeekBar>(R.id.vMarginSeekBar)
        val vMarginValue = view.findViewById<TextView>(R.id.vMarginValue)
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
            view.findViewById(R.id.blockCountSpinner), blockCountOptions.map { it.second },
            blockCountOptions.indexOfFirst { it.first == prefs.getInt(MainActivity.PREF_BLOCK_COUNT, MainActivity.DEFAULT_BLOCK_COUNT) }
                .coerceAtLeast(0)
        ) { pos ->
            prefs.edit().putInt(MainActivity.PREF_BLOCK_COUNT, blockCountOptions[pos].first).apply()
        }

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
        val installedPacks = IconPackResolver.getInstalledPacks(requireContext())
        val iconPackOptions = mutableListOf("" to "Default")
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
