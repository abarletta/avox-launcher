package com.avox.launcher

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}