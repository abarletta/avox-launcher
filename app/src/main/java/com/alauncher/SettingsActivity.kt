package com.alauncher

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

        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
        findViewById<View>(R.id.settingsOverlay).alpha =
            prefs.getInt(MainActivity.PREF_DARKNESS, MainActivity.DEFAULT_DARKNESS) / 100f

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
