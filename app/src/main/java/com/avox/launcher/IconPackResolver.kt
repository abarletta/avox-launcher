package com.avox.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser

class IconPackResolver(private val context: Context) {

    private var packPackage: String? = null
    private var packResources: Resources? = null
    private val componentMap = mutableMapOf<String, String>()

    fun load(packPkg: String): Boolean {
        componentMap.clear()
        packPackage = null
        packResources = null
        return try {
            val res = context.packageManager.getResourcesForApplication(packPkg)
            packPackage = packPkg
            packResources = res
            parseAppFilter(packPkg, res)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resolve(packageName: String): Drawable? {
        val res = packResources ?: return null
        val pkg = packPackage ?: return null

        // Try launch activity component
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent?.component != null) {
            val key = launchIntent.component!!.flattenToString()
            val drawableName = componentMap[key]
            if (drawableName != null) {
                val d = loadDrawable(res, pkg, drawableName)
                if (d != null) return d
            }
        }

        // Try any matching component for the package
        for ((comp, drawableName) in componentMap) {
            if (comp.startsWith("$packageName/")) {
                val d = loadDrawable(res, pkg, drawableName)
                if (d != null) return d
            }
        }

        return null
    }

    private fun loadDrawable(res: Resources, pkg: String, name: String): Drawable? {
        return try {
            val id = res.getIdentifier(name, "drawable", pkg)
            if (id != 0) res.getDrawable(id, null) else null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAppFilter(pkg: String, res: Resources) {
        try {
            val id = res.getIdentifier("appfilter", "xml", pkg)
            if (id != 0) {
                val parser = res.getXml(id)
                parseXml(parser)
                return
            }
        } catch (_: Exception) {}

        // Fallback: try assets
        try {
            val am = context.packageManager.getResourcesForApplication(pkg).assets
            am.open("appfilter.xml").use { stream ->
                val parser = android.util.Xml.newPullParser()
                parser.setInput(stream, null)
                parseXml(parser)
            }
        } catch (_: Exception) {}
    }

    private fun parseXml(parser: XmlPullParser) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val component = parser.getAttributeValue(null, "component")
                val drawable = parser.getAttributeValue(null, "drawable")
                if (component != null && drawable != null) {
                    // Component format: ComponentInfo{pkg/cls}
                    val cleaned = component
                        .removePrefix("ComponentInfo{")
                        .removeSuffix("}")
                    if (cleaned.contains("/")) {
                        componentMap[cleaned] = drawable
                    }
                }
            }
            eventType = parser.next()
        }
    }

    companion object {
        fun getInstalledPacks(context: Context): List<Pair<String, String>> {
            val pm = context.packageManager
            val packs = mutableListOf<Pair<String, String>>()
            val seen = mutableSetOf<String>()

            val actions = listOf(
                "org.adw.launcher.THEMES",
                "com.novalauncher.THEME",
                "com.teslacoilsw.launcher.THEME",
                "com.gau.go.launcherex.theme"
            )

            for (action in actions) {
                try {
                    val intent = Intent(action)
                    val infos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
                    for (info in infos) {
                        val pkg = info.activityInfo.packageName
                        if (pkg !in seen) {
                            seen.add(pkg)
                            val label = info.loadLabel(pm).toString()
                            packs.add(pkg to label)
                        }
                    }
                } catch (_: Exception) {}
            }

            return packs.sortedBy { it.second }
        }
    }
}
