package com.avox.launcher

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @Test
    fun launchesSettingsMenuByDefault() {
        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()

                assertNotNull(activity.findViewById<android.view.View>(R.id.settingsContainer))
                assertTrue(
                    activity.supportFragmentManager.findFragmentById(R.id.settingsContainer) is SettingsMenuFragment
                )
            }
        }
    }

    @Test
    fun appliesColorTintOverlayFromPrefs() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString(MainActivity.PREF_WALLPAPER_EFFECT, MainActivity.WALLPAPER_EFFECT_COLOR)
            .putString(MainActivity.PREF_COLOR_TINT, MainActivity.DEFAULT_COLOR_TINT)
            .putInt(MainActivity.PREF_DARKNESS, 60)
            .commit()

        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val overlay = activity.findViewById<android.view.View>(R.id.settingsOverlay)
                val color = (overlay.background as ColorDrawable).color
                val parsed = Color.parseColor(MainActivity.DEFAULT_COLOR_TINT)
                val expected = Color.argb(
                    (0.6f * 255).toInt(),
                    Color.red(parsed),
                    Color.green(parsed),
                    Color.blue(parsed)
                )

                assertEquals(expected, color)
                assertEquals(1f, overlay.alpha)
            }
        }
    }
}