package com.avox.launcher

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.io.File

class LauncherApp : Application() {

    companion object {
        const val LANGUAGE_TAG_SYSTEM = ""
        const val LANGUAGE_TAG_ENGLISH = "en"

        fun applyLanguagePreference(context: Context): Boolean {
            val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val languageTag = prefs.getString(MainActivity.PREF_LANGUAGE, LANGUAGE_TAG_SYSTEM)
                ?: LANGUAGE_TAG_SYSTEM
            val desiredTags = languageTag.trim()
            if (AppCompatDelegate.getApplicationLocales().toLanguageTags() == desiredTags) {
                return false
            }

            val locales = if (desiredTags.isBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(desiredTags)
            }
            AppCompatDelegate.setApplicationLocales(locales)
            return true
        }
    }

    var customTypeface: Typeface? = null
        private set

    override fun onCreate() {
        super.onCreate()
        applyLanguagePreference(this)
        loadCustomFont()
    }

    fun loadCustomFont() {
        val fontFile = File(filesDir, "custom_font.ttf")
        customTypeface = if (fontFile.exists()) {
            try {
                Typeface.createFromFile(fontFile)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun resolveTypeface(fontFamily: String?): Typeface {
        val resolvedFamily = fontFamily?.takeUnless { it.isBlank() } ?: MainActivity.DEFAULT_FONT
        if (resolvedFamily == MainActivity.CUSTOM_FONT_KEY) {
            return customTypeface ?: Typeface.create(MainActivity.DEFAULT_FONT, Typeface.NORMAL)
        }
        return Typeface.create(MainActivity.DEFAULT_FONT, Typeface.NORMAL)
    }
}
