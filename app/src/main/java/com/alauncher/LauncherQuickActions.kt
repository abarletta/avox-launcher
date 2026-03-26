package com.alauncher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.provider.Settings

data class FooterQuickAction(
    val spec: String,
    val label: String,
    val icon: Drawable?,
    val intent: Intent,
    val packageName: String? = null
)

object LauncherQuickActions {
    const val SLOT_COUNT = 3

    private const val APP_PREFIX = "app:"

    const val SPEC_LAUNCHER_SETTINGS = "launcher_settings"
    const val SPEC_SYSTEM_SETTINGS = "system_settings"
    const val SPEC_WIFI_SETTINGS = "wifi_settings"
    const val SPEC_BLUETOOTH_SETTINGS = "bluetooth_settings"
    const val SPEC_DISPLAY_SETTINGS = "display_settings"
    const val SPEC_APPLICATION_SETTINGS = "application_settings"
    const val SPEC_PICK_APP = "pick_app"

    private const val DEFAULT_FIRST_SLOT = SPEC_LAUNCHER_SETTINGS

    data class Choice(val spec: String, val label: String)

    fun getSpec(prefs: SharedPreferences, index: Int): String? {
        val stored = prefs.getString(slotKey(index), null)
        return when {
            stored != null -> stored.ifBlank { null }
            index == 0 -> DEFAULT_FIRST_SLOT
            else -> null
        }
    }

    fun setSpec(prefs: SharedPreferences, index: Int, spec: String?) {
        prefs.edit().putString(slotKey(index), spec.orEmpty()).apply()
    }

    fun buildAppSpec(packageName: String): String = APP_PREFIX + packageName

    fun loadLaunchableApps(context: Context): List<Pair<String, String>> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val packageName = activityInfo.packageName
                val label = resolveInfo.loadLabel(pm)?.toString().orEmpty().ifBlank { packageName }
                packageName to label
            }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }
    }

    fun getChoices(context: Context): List<Choice> = listOf(
        Choice(SPEC_LAUNCHER_SETTINGS, context.getString(R.string.quick_action_launcher_settings)),
        Choice(SPEC_SYSTEM_SETTINGS, context.getString(R.string.quick_action_system_settings)),
        Choice(SPEC_WIFI_SETTINGS, context.getString(R.string.quick_action_wifi_settings)),
        Choice(SPEC_BLUETOOTH_SETTINGS, context.getString(R.string.quick_action_bluetooth_settings)),
        Choice(SPEC_DISPLAY_SETTINGS, context.getString(R.string.quick_action_display_settings)),
        Choice(SPEC_APPLICATION_SETTINGS, context.getString(R.string.quick_action_app_settings)),
        Choice(SPEC_PICK_APP, context.getString(R.string.quick_action_open_app))
    )

    fun getDisplayLabel(context: Context, prefs: SharedPreferences, index: Int): String {
        val spec = getSpec(prefs, index) ?: return context.getString(R.string.footer_action_not_set)
        return resolveAction(context, spec)?.label ?: context.getString(R.string.footer_action_unavailable)
    }

    fun resolveAction(context: Context, spec: String?): FooterQuickAction? {
        if (spec.isNullOrBlank()) return null
        if (spec.startsWith(APP_PREFIX)) {
            val packageName = spec.removePrefix(APP_PREFIX)
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName) ?: return null
            return try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val label = pm.getApplicationLabel(appInfo)?.toString().orEmpty().ifBlank { packageName }
                FooterQuickAction(
                    spec = spec,
                    label = label,
                    icon = pm.getApplicationIcon(packageName),
                    intent = launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    packageName = packageName
                )
            } catch (_: Exception) {
                null
            }
        }

        val targetIntent = when (spec) {
            SPEC_LAUNCHER_SETTINGS -> Intent(context, SettingsActivity::class.java)
            SPEC_SYSTEM_SETTINGS -> Intent(Settings.ACTION_SETTINGS)
            SPEC_WIFI_SETTINGS -> Intent(Settings.ACTION_WIFI_SETTINGS)
            SPEC_BLUETOOTH_SETTINGS -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            SPEC_DISPLAY_SETTINGS -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            SPEC_APPLICATION_SETTINGS -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
            else -> null
        } ?: return null

        val safeIntent = if (targetIntent.resolveActivity(context.packageManager) != null) {
            targetIntent
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val labelRes = when (spec) {
            SPEC_LAUNCHER_SETTINGS -> R.string.quick_action_launcher_settings
            SPEC_SYSTEM_SETTINGS -> R.string.quick_action_system_settings
            SPEC_WIFI_SETTINGS -> R.string.quick_action_wifi_settings
            SPEC_BLUETOOTH_SETTINGS -> R.string.quick_action_bluetooth_settings
            SPEC_DISPLAY_SETTINGS -> R.string.quick_action_display_settings
            SPEC_APPLICATION_SETTINGS -> R.string.quick_action_app_settings
            else -> return null
        }

        return FooterQuickAction(
            spec = spec,
            label = context.getString(labelRes),
            icon = context.getDrawable(R.drawable.ic_settings),
            intent = safeIntent
        )
    }

    private fun slotKey(index: Int): String = "footer_action_$index"
}