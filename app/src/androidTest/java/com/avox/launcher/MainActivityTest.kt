package com.avox.launcher

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun launchesMainActivityWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity.findViewById(R.id.rootLayout))
                assertNotNull(activity.findViewById(R.id.appList))
                assertNotNull(activity.findViewById(R.id.favoritesGrid))
                assertNotNull(activity.findViewById(R.id.alphabetSidebar))
            }
        }
    }
}
