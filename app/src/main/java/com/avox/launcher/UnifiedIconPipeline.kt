package com.avox.launcher

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.json.JSONObject

object UnifiedIconPipeline {

    private var iconPackResolver: IconPackResolver? = null
    private var builtInStyle: String = "rounded"
    private var builtInColor: String = "dark"
    private var iconOverrides = mutableMapOf<String, String>()

    // Categories
    val CATEGORIES = listOf(
        "phone", "message", "contacts", "email", "shopping", "finance", "document", "calendar",
        "settings", "gamepad", "tools", "health", "travel/luggage", "music", "movie/video",
        "education", "gallery/photos", "baby/parenting", "security/locker", "parcel/delivery",
        "password/credentials", "web browser", "file manager", "camera", "social media",
        "bolt/flash", "star", "heart", "cloud", "eye/view", "food", "maps/navigation",
        "news/reading", "sports/activity"
    )

    private val pkgToCategoryMap = mapOf(
        "com.android.dialer" to "phone",
        "com.google.android.dialer" to "phone",
        "com.android.messaging" to "message",
        "com.google.android.apps.messaging" to "message",
        "com.android.contacts" to "contacts",
        "com.google.android.contacts" to "contacts",
        "com.google.android.gm" to "email",
        "com.android.email" to "email",
        "com.android.vending" to "shopping",
        "com.amazon.mShop.android.shopping" to "shopping",
        "com.google.android.calendar" to "calendar",
        "com.android.calendar" to "calendar",
        "com.android.settings" to "settings",
        "com.android.chrome" to "web browser",
        "org.mozilla.firefox" to "web browser",
        "com.google.android.youtube" to "movie/video",
        "com.google.android.apps.maps" to "maps/navigation",
        "com.google.android.apps.photos" to "gallery/photos",
        "com.android.camera2" to "camera"
    )

    fun init(context: Context, prefs: android.content.SharedPreferences) {
        val pack = prefs.getString(MainActivity.PREF_ICON_PACK, "") ?: ""
        if (pack.isNotBlank()) {
            if (iconPackResolver == null || iconPackResolver?.loadedPackage() != pack) {
                iconPackResolver = IconPackResolver(context).apply { load(pack) }
            }
        } else {
            iconPackResolver = null
        }

        builtInStyle = prefs.getString("builtin_icon_style", "rounded") ?: "rounded"
        builtInColor = prefs.getString("builtin_icon_color", "dark") ?: "dark"

        val overridesStr = prefs.getString("icon_overrides", "{}") ?: "{}"
        iconOverrides.clear()
        try {
            val json = JSONObject(overridesStr)
            for (key in json.keys()) {
                iconOverrides[key] = json.getString(key)
            }
        } catch (_: Exception) {}
    }

    fun resolveIcon(context: Context, packageName: String, launchIntent: android.content.Intent?): Drawable? {
        val overrideSpec = iconOverrides[packageName]
        if (overrideSpec != null) {
            val overrideIcon = resolveSpec(context, overrideSpec, packageName)
            if (overrideIcon != null) return overrideIcon
        }

        val packIcon = iconPackResolver?.resolve(packageName)
        if (packIcon != null) return packIcon

        val category = guessCategory(packageName)
        if (category != null) {
            return generateBuiltInIcon(context, category, builtInStyle, builtInColor)
        }

        if (launchIntent != null && launchIntent.component != null) {
            return try {
                context.packageManager.getActivityIcon(launchIntent.component!!)
            } catch (_: Exception) {
                context.packageManager.getApplicationIcon(packageName)
            }
        }
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            context.packageManager.defaultActivityIcon
        }
    }

    fun setOverride(context: Context, prefs: android.content.SharedPreferences, packageName: String, spec: String?) {
        if (spec == null) {
            iconOverrides.remove(packageName)
        } else {
            iconOverrides[packageName] = spec
        }
        val json = JSONObject()
        for ((k, v) in iconOverrides) {
            json.put(k, v)
        }
        prefs.edit().putString("icon_overrides", json.toString()).apply()
    }

    fun generateBuiltInIcon(context: Context, category: String, style: String, colorTheme: String): Drawable {
        val size = (56 * context.resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val bgColor = when (colorTheme) {
            "dark" -> Color.parseColor("#333333")
            "light" -> Color.parseColor("#EEEEEE")
            "blue" -> Color.parseColor("#1976D2")
            "red" -> Color.parseColor("#D32F2F")
            "green" -> Color.parseColor("#388E3C")
            "yellow" -> Color.parseColor("#FBC02D")
            "violet" -> Color.parseColor("#7B1FA2")
            else -> Color.parseColor("#333333")
        }

        val fgColor = if (colorTheme == "light") Color.BLACK else Color.WHITE

        paint.color = bgColor
        if (style == "outline") {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = size * 0.05f
            paint.color = fgColor
        } else {
            paint.style = Paint.Style.FILL
        }

        val padding = size * 0.1f
        val rect = RectF(padding, padding, size - padding, size - padding)

        when (style) {
            "rounded", "outline" -> canvas.drawRoundRect(rect, size * 0.2f, size * 0.2f, paint)
            "square" -> canvas.drawRect(rect, paint)
            "full" -> {
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            }
        }

        paint.style = Paint.Style.FILL
        paint.color = fgColor
        paint.textSize = size * 0.4f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        val letter = getCategoryLetter(category)
        val fontMetrics = paint.fontMetrics
        val y = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(letter, rect.centerX(), y, paint)

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun getCategoryLetter(category: String): String {
        return when (category) {
            "phone" -> "P"
            "message" -> "M"
            "contacts" -> "C"
            "email" -> "@"
            "shopping" -> "S"
            "finance" -> "$"
            "document" -> "D"
            "calendar" -> "31"
            "settings" -> "O"
            "gamepad" -> "G"
            "tools" -> "T"
            "health" -> "+"
            "travel/luggage" -> "Tr"
            "music" -> "♪"
            "movie/video" -> "V"
            "education" -> "E"
            "gallery/photos" -> "Ph"
            "baby/parenting" -> "B"
            "security/locker" -> "L"
            "parcel/delivery" -> "Bx"
            "password/credentials" -> "*"
            "web browser" -> "W"
            "file manager" -> "F"
            "camera" -> "Cam"
            "social media" -> "#"
            "bolt/flash" -> "⚡"
            "star" -> "★"
            "heart" -> "♥"
            "cloud" -> "☁"
            "eye/view" -> "👁"
            "food" -> "Fd"
            "maps/navigation" -> "N"
            "news/reading" -> "Nw"
            "sports/activity" -> "Sp"
            else -> category.firstOrNull()?.uppercase() ?: "?"
        }
    }

    private fun resolveSpec(context: Context, spec: String, packageName: String): Drawable? {
        if (spec == "default") return null
        if (spec.startsWith("builtin:")) {
            val parts = spec.removePrefix("builtin:").split("/")
            if (parts.size == 3) {
                return generateBuiltInIcon(context, parts[0], parts[1], parts[2])
            } else if (parts.size == 1) {
                return generateBuiltInIcon(context, parts[0], builtInStyle, builtInColor)
            }
        }
        if (spec.startsWith("pack:")) {
            val packPkg = spec.removePrefix("pack:")
            val resolver = IconPackResolver(context).apply { load(packPkg) }
            return resolver.resolve(packageName)
        }
        return null
    }

    private fun guessCategory(packageName: String): String? {
        return pkgToCategoryMap[packageName]
    }
}
