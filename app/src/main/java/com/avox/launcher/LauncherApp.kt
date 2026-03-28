package com.avox.launcher

import android.app.Application
import android.graphics.Typeface
import java.io.File

class LauncherApp : Application() {

    var customTypeface: Typeface? = null
        private set

    override fun onCreate() {
        super.onCreate()
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
}
