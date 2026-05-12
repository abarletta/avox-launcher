package com.avox.launcher

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class AppFilteringTest {

    @Test
    fun appFiltering_filtersByLabelIgnoreCase() {
        val app1 = AppInfo("Calculator", "com.calc", mock<Intent>())
        val app2 = AppInfo("Phone", "com.phone", mock<Intent>())
        val app3 = AppInfo("Chrome", "com.chrome", mock<Intent>())
        
        val allApps = listOf(app1, app2, app3)
        
        val query = "c"
        val filtered = allApps.filter { it.label.contains(query, ignoreCase = true) }
        
        assertEquals(2, filtered.size)
        assert(filtered.contains(app1))
        assert(filtered.contains(app3))
    }

    @Test
    fun appFiltering_returnsEmptyOnNoMatch() {
        val app1 = AppInfo("Calculator", "com.calc", mock<Intent>())
        val allApps = listOf(app1)
        
        val query = "xyz"
        val filtered = allApps.filter { it.label.contains(query, ignoreCase = true) }
        
        assert(filtered.isEmpty())
    }
}
