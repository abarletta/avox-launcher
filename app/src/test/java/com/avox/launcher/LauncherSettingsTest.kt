package com.avox.launcher

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherSettingsTest {

    @Test
    fun defaultRestorablePreferences_includeRuntimeSpacingDefault() {
        val defaults = LauncherSettings.defaultRestorablePreferences()

        assertEquals(MainActivity.DEFAULT_SPACING, defaults[MainActivity.PREF_SPACING])
    }

    @Test
    fun applyRestorablePreferences_normalizesUnsupportedValues() {
        val prefs = InMemorySharedPreferences()

        LauncherSettings.applyRestorablePreferences(
            prefs,
            linkedMapOf(
                MainActivity.PREF_SPACING to 999,
                MainActivity.PREF_THEME to "unsupported",
                MainActivity.PREF_ALIGNMENT to "unsupported"
            )
        )

        assertEquals(MainActivity.DEFAULT_SPACING, prefs.getInt(MainActivity.PREF_SPACING, -1))
        assertEquals("system", prefs.getString(MainActivity.PREF_THEME, null))
        assertEquals("left", prefs.getString(MainActivity.PREF_ALIGNMENT, null))
    }

    @Test
    fun applyRestorablePreferences_preservesWidgetPrefsWhenIncomingStateOmitsWidgets() {
        val prefs = InMemorySharedPreferences()
        prefs.edit()
            .putString(MainActivity.PREF_WIDGET_ORDER, "slots:101")
            .putInt("widget_h_101", 180)
            .putBoolean("widget_fw_101", true)
            .commit()

        val mergedDefaults = LauncherSettings.defaultRestorablePreferences().apply {
            put(MainActivity.PREF_FONT_SIZE, 30)
        }

        LauncherSettings.applyRestorablePreferences(prefs, mergedDefaults)

        assertEquals("slots:101", prefs.getString(MainActivity.PREF_WIDGET_ORDER, null))
        assertEquals(180, prefs.getInt("widget_h_101", -1))
        assertTrue(prefs.getBoolean("widget_fw_101", false))
        assertEquals(30, prefs.getInt(MainActivity.PREF_FONT_SIZE, -1))
    }

    private class InMemorySharedPreferences : SharedPreferences {
        private val values = linkedMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = values.toMutableMap()

        override fun getString(key: String?, defValue: String?): String? {
            return values[key] as? String ?: defValue
        }

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
            val stored = values[key] as? Set<String>
            return stored?.toMutableSet() ?: defValues
        }

        override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue

        override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue

        override fun contains(key: String?): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor(values)

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        private class Editor(
            private val values: LinkedHashMap<String, Any?>
        ) : SharedPreferences.Editor {
            private val pending = linkedMapOf<String, Any?>()
            private var clearRequested = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor = applyChange(key, value)

            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                return applyChange(key, values?.toSet())
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = applyChange(key, value)

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = applyChange(key, value)

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = applyChange(key, value)

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = applyChange(key, value)

            override fun remove(key: String?): SharedPreferences.Editor = applyChange(key, null)

            override fun clear(): SharedPreferences.Editor {
                clearRequested = true
                pending.clear()
                return this
            }

            override fun commit(): Boolean {
                if (clearRequested) {
                    values.clear()
                }
                pending.forEach { (key, value) ->
                    if (value == null) {
                        values.remove(key)
                    } else {
                        values[key] = value
                    }
                }
                pending.clear()
                clearRequested = false
                return true
            }

            override fun apply() {
                commit()
            }

            private fun applyChange(key: String?, value: Any?): SharedPreferences.Editor {
                requireNotNull(key)
                pending[key] = value
                return this
            }
        }
    }
}