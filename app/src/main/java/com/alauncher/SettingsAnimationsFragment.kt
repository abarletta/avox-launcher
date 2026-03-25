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
import androidx.fragment.app.Fragment

class SettingsAnimationsFragment : Fragment() {

    private val animStyleOptions = listOf(
        AlphabetSidebar.STYLE_WAVE to "Wave / Zoom",
        AlphabetSidebar.STYLE_HIGHLIGHT to "Highlight",
        AlphabetSidebar.STYLE_FADE to "Fade"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings_animations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        val waveControls = view.findViewById<View>(R.id.waveControlsContainer)
        val highlightControls = view.findViewById<View>(R.id.highlightControlsContainer)
        val fadeControls = view.findViewById<View>(R.id.fadeControlsContainer)

        fun updateAnimControls(style: String) {
            waveControls.visibility = if (style == AlphabetSidebar.STYLE_WAVE) View.VISIBLE else View.GONE
            highlightControls.visibility = if (style == AlphabetSidebar.STYLE_HIGHLIGHT) View.VISIBLE else View.GONE
            fadeControls.visibility = if (style == AlphabetSidebar.STYLE_FADE) View.VISIBLE else View.GONE
        }

        val currentAnimStyle = prefs.getString(MainActivity.PREF_ANIM_STYLE, AlphabetSidebar.STYLE_WAVE) ?: AlphabetSidebar.STYLE_WAVE
        updateAnimControls(currentAnimStyle)

        setupSpinner(
            view.findViewById(R.id.animStyleSpinner), animStyleOptions.map { it.second },
            animStyleOptions.indexOfFirst { it.first == currentAnimStyle }.coerceAtLeast(0)
        ) { pos ->
            val style = animStyleOptions[pos].first
            prefs.edit().putString(MainActivity.PREF_ANIM_STYLE, style).apply()
            updateAnimControls(style)
        }

        // Wave shift
        val waveShiftSeekBar = view.findViewById<SeekBar>(R.id.waveShiftSeekBar)
        val waveShiftValue = view.findViewById<TextView>(R.id.waveShiftValue)
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
        val waveScaleSeekBar = view.findViewById<SeekBar>(R.id.waveScaleSeekBar)
        val waveScaleValue = view.findViewById<TextView>(R.id.waveScaleValue)
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
        val highlightSeekBar = view.findViewById<SeekBar>(R.id.highlightIntensitySeekBar)
        val highlightValue = view.findViewById<TextView>(R.id.highlightIntensityValue)
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
        val fadeSeekBar = view.findViewById<SeekBar>(R.id.fadeRadiusSeekBar)
        val fadeValue = view.findViewById<TextView>(R.id.fadeRadiusValue)
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
