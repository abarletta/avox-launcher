package com.avox.launcher

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LauncherQuickActionsTest {

    @Test
    fun getSpec_returnsDefaultForFirstSlotWhenUnset() {
        val prefs = InMemorySharedPreferences()

        assertEquals(
            LauncherQuickActions.SPEC_LAUNCHER_SETTINGS,
            LauncherQuickActions.getSpec(prefs, 0)
        )
    }

    @Test
    fun getSpec_returnsNullForSecondarySlotWhenUnsetOrCleared() {
        val prefs = InMemorySharedPreferences()

        assertNull(LauncherQuickActions.getSpec(prefs, 1))

        LauncherQuickActions.setSpec(prefs, 1, null)

        assertNull(LauncherQuickActions.getSpec(prefs, 1))
    }

    @Test
    fun setSpec_roundTripsStoredValue() {
        val prefs = InMemorySharedPreferences()
        val expected = LauncherQuickActions.buildAppSpec("com.example.clock")

        LauncherQuickActions.setSpec(prefs, 2, expected)

        assertEquals(expected, LauncherQuickActions.getSpec(prefs, 2))
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