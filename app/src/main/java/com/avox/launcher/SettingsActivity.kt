package com.avox.launcher

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPEN_SCREEN = "open_screen"
        const val EXTRA_FOOTER_SLOT_INDEX = "footer_slot_index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPrefs()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        applyWallpaperOverlay()

        if (savedInstanceState == null) {
            val initialFragment = when (intent.getStringExtra(EXTRA_OPEN_SCREEN)) {
                SettingsSystemFragment.MODE_HOME,
                SettingsSystemFragment.MODE_WIDGETS_HOME -> SettingsSystemFragment.newInstance(SettingsSystemFragment.MODE_HOME)
                SettingsSystemFragment.MODE_WIDGETS -> SettingsSystemFragment.newInstance(SettingsSystemFragment.MODE_WIDGETS)
                SettingsSystemFragment.MODE_NOTIFICATIONS -> SettingsSystemFragment.newInstance(SettingsSystemFragment.MODE_NOTIFICATIONS)
                else -> SettingsMenuFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.settingsContainer, initialFragment)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        applyWallpaperOverlay()
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right
            )
            .replace(R.id.settingsContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    fun applyWallpaperOverlay() {
        val overlay = findViewById<View>(R.id.settingsOverlay)
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val effect = prefs.getString(MainActivity.PREF_WALLPAPER_EFFECT, MainActivity.WALLPAPER_EFFECT_DARKEN)
            ?: MainActivity.WALLPAPER_EFFECT_DARKEN
        val darkness = prefs.getInt(MainActivity.PREF_DARKNESS, MainActivity.DEFAULT_DARKNESS) / 100f

        when (effect) {
            MainActivity.WALLPAPER_EFFECT_COLOR -> {
                val tintColor = prefs.getString(MainActivity.PREF_COLOR_TINT, MainActivity.DEFAULT_COLOR_TINT)
                    ?: MainActivity.DEFAULT_COLOR_TINT
                try {
                    val parsed = Color.parseColor(tintColor)
                    val alpha = (darkness.coerceAtLeast(0.35f) * 255).toInt().coerceIn(0, 255)
                    overlay.setBackgroundColor(
                        Color.argb(alpha, Color.red(parsed), Color.green(parsed), Color.blue(parsed))
                    )
                    overlay.alpha = 1f
                } catch (_: IllegalArgumentException) {
                    overlay.setBackgroundColor(Color.BLACK)
                    overlay.alpha = darkness
                }
            }
            else -> {
                overlay.setBackgroundColor(Color.BLACK)
                overlay.alpha = darkness
            }
        }
    }

    private fun applyThemeFromPrefs() {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        val theme = prefs.getString(MainActivity.PREF_THEME, "system") ?: "system"
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
