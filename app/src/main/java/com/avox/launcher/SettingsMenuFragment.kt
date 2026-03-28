package com.avox.launcher

import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class SettingsMenuFragment : Fragment() {

    private data class BackupPreference(
        val type: String,
        val value: Any
    )

    private data class ParsedSettingsBackup(
        val version: Int,
        val preferences: LinkedHashMap<String, BackupPreference>,
        val widgetRestorePlanJson: String? = null
    )

    private val backupDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            saveSettingsBackup(uri)
        }
    }

    private val restoreDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            confirmRestoreSettings(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = requireActivity() as SettingsActivity
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val codes = resources.getStringArray(R.array.language_codes).toList()
        val labels = resources.getStringArray(R.array.language_labels).toList()

        val selectedIndex = codes.indexOf(
            prefs.getString(MainActivity.PREF_LANGUAGE, LauncherApp.LANGUAGE_TAG_SYSTEM)
                ?: LauncherApp.LANGUAGE_TAG_SYSTEM
        ).coerceAtLeast(0)

        setupSpinner(
            view.findViewById(R.id.languageSpinner),
            labels,
            selectedIndex
        ) { pos ->
            val selectedLanguage = codes.getOrNull(pos) ?: LauncherApp.LANGUAGE_TAG_SYSTEM
            val savedLanguage = prefs.getString(MainActivity.PREF_LANGUAGE, LauncherApp.LANGUAGE_TAG_SYSTEM)
                ?: LauncherApp.LANGUAGE_TAG_SYSTEM
            if (selectedLanguage == savedLanguage) return@setupSpinner

            prefs.edit().putString(MainActivity.PREF_LANGUAGE, selectedLanguage).apply()
            LauncherApp.applyLanguagePreference(requireContext())
        }

        view.findViewById<View>(R.id.cardAppearance).setOnClickListener { activity.showFragment(SettingsAppearanceFragment()) }
        view.findViewById<View>(R.id.cardWallpaper).setOnClickListener { activity.showFragment(SettingsWallpaperFragment()) }
        view.findViewById<View>(R.id.cardAnimations).setOnClickListener { activity.showFragment(SettingsAnimationsFragment()) }
        view.findViewById<View>(R.id.cardNotifications).setOnClickListener {
            activity.showFragment(SettingsSystemFragment.newInstance(SettingsSystemFragment.MODE_NOTIFICATIONS))
        }
        view.findViewById<View>(R.id.cardHome).setOnClickListener {
            activity.showFragment(SettingsSystemFragment.newInstance(SettingsSystemFragment.MODE_HOME))
        }
        view.findViewById<View>(R.id.cardWidgets).setOnClickListener {
            activity.showFragment(SettingsSystemFragment.newInstance(SettingsSystemFragment.MODE_WIDGETS))
        }
        view.findViewById<Button>(R.id.backupSettingsButton).setOnClickListener {
            backupDocumentLauncher.launch(getString(R.string.launcher_backup_file_name))
        }
        view.findViewById<Button>(R.id.restoreSettingsButton).setOnClickListener {
            restoreDocumentLauncher.launch(arrayOf("application/json", "text/plain"))
        }
    }

    private fun saveSettingsBackup(uri: Uri) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val payload = buildSettingsBackupJson(prefs).toString(2)
            requireContext().contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                writer.write(payload)
            } ?: error("Unable to open backup destination")
            Toast.makeText(requireContext(), R.string.backup_settings_success, Toast.LENGTH_SHORT).show()
            promptShareSettingsBackup(uri)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.backup_settings_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmRestoreSettings(uri: Uri) {
        try {
            val parsedBackup = loadSettingsBackup(uri)
            val fileName = resolveDisplayName(uri)
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_settings_confirm_title)
                .setMessage(
                    getString(
                        R.string.restore_settings_confirm_message,
                        fileName,
                        parsedBackup.preferences.size
                    )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.restore_settings_confirm_action) { _, _ ->
                    performRestoreSettingsBackup(parsedBackup)
                }
                .show()
        } catch (_: UnsupportedOperationException) {
            Toast.makeText(requireContext(), R.string.restore_settings_incompatible, Toast.LENGTH_SHORT).show()
        } catch (_: org.json.JSONException) {
            Toast.makeText(requireContext(), R.string.restore_settings_invalid, Toast.LENGTH_SHORT).show()
        } catch (_: IllegalArgumentException) {
            Toast.makeText(requireContext(), R.string.restore_settings_invalid, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.restore_settings_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRestoreSettingsBackup(parsedBackup: ParsedSettingsBackup) {
        try {
            applySettingsBackup(parsedBackup)
            val languageChanged = LauncherApp.applyLanguagePreference(requireContext())
            Toast.makeText(requireContext(), R.string.restore_settings_success, Toast.LENGTH_SHORT).show()
            if (!parsedBackup.widgetRestorePlanJson.isNullOrBlank()) {
                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                })
                requireActivity().finish()
            } else if (!languageChanged) {
                requireActivity().recreate()
            }
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.restore_settings_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildSettingsBackupJson(prefs: SharedPreferences): JSONObject {
        val preferencesJson = JSONObject()
        prefs.all.toSortedMap().forEach { (key, value) ->
            if (!isRestorablePreferenceKey(key) || value == null) {
                return@forEach
            }
            val entry = JSONObject()
            when (value) {
                is Boolean -> {
                    entry.put("type", "boolean")
                    entry.put("value", value)
                }
                is Int -> {
                    entry.put("type", "int")
                    entry.put("value", value)
                }
                is Long -> {
                    entry.put("type", "long")
                    entry.put("value", value)
                }
                is Float -> {
                    entry.put("type", "float")
                    entry.put("value", value.toDouble())
                }
                is String -> {
                    entry.put("type", "string")
                    entry.put("value", value)
                }
                is Set<*> -> {
                    entry.put("type", "string_set")
                    entry.put(
                        "value",
                        JSONArray(value.filterIsInstance<String>().sorted())
                    )
                }
                else -> return@forEach
            }
            preferencesJson.put(key, entry)
        }

        return JSONObject()
            .put("format", SETTINGS_BACKUP_FORMAT)
            .put("version", SETTINGS_BACKUP_VERSION)
            .put("preferences", preferencesJson)
            .apply {
                buildWidgetMetadataJson(prefs)?.let { put("widgets", it) }
            }
    }

    private fun buildWidgetMetadataJson(prefs: SharedPreferences): JSONObject? {
        val serializedOrder = prefs.getString(MainActivity.PREF_WIDGET_ORDER, null)
        val slots = MainActivity.parseWidgetSlots(serializedOrder)
        if (slots.isEmpty()) return null

        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val packageManager = requireContext().packageManager
        val slotsJson = JSONArray()

        slots.forEachIndexed { slotIndex, slot ->
            if (slot.widgetIds.isEmpty()) return@forEachIndexed

            val widgetsJson = JSONArray()
            val safeActiveIndex = slot.activeIndex.coerceIn(0, slot.widgetIds.lastIndex)
            slot.widgetIds.forEachIndexed { widgetIndex, widgetId ->
                val info = appWidgetManager.getAppWidgetInfo(widgetId)
                val widgetJson = JSONObject()
                    .put("appWidgetId", widgetId)
                    .put("isActive", widgetIndex == safeActiveIndex)
                    .put("fullWidth", prefs.getBoolean("widget_fw_$widgetId", false))

                val heightPx = prefs.getInt("widget_h_$widgetId", -1)
                if (heightPx > 0) {
                    widgetJson.put("heightPx", heightPx)
                }

                if (info != null) {
                    widgetJson.put("provider", info.provider.flattenToString())
                    widgetJson.put("providerPackage", info.provider.packageName)
                    widgetJson.put("providerClass", info.provider.className)
                    val widgetLabel = info.loadLabel(packageManager)?.toString().orEmpty()
                    if (widgetLabel.isNotBlank()) {
                        widgetJson.put("label", widgetLabel)
                    }
                    val providerAppLabel = try {
                        val appInfo = packageManager.getApplicationInfo(info.provider.packageName, 0)
                        packageManager.getApplicationLabel(appInfo)?.toString()
                    } catch (_: Exception) {
                        null
                    }
                    if (!providerAppLabel.isNullOrBlank()) {
                        widgetJson.put("providerApp", providerAppLabel)
                    }
                } else {
                    widgetJson.put("provider", JSONObject.NULL)
                    widgetJson.put("bindingState", "missing")
                }

                widgetsJson.put(widgetJson)
            }

            slotsJson.put(
                JSONObject()
                    .put("slotIndex", slotIndex)
                    .put("activeIndex", safeActiveIndex)
                    .put("widgets", widgetsJson)
            )
        }

        if (slotsJson.length() == 0) return null

        return JSONObject()
            .put("serializedOrder", serializedOrder ?: "")
            .put("slots", slotsJson)
    }

    private fun loadSettingsBackup(uri: Uri): ParsedSettingsBackup {
        val content = requireContext().contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
            it.readText()
        } ?: error("Unable to open restore source")
        return parseSettingsBackup(parseBackupRootObject(content))
    }

    private fun parseBackupRootObject(content: String): JSONObject {
        val parsed = JSONTokener(content).nextValue() as? JSONObject
            ?: throw IllegalArgumentException("Backup root must be a JSON object")
        return parsed
    }

    private fun parseSettingsBackup(root: JSONObject): ParsedSettingsBackup {
        val version = if (root.has("version")) root.optInt("version", -1) else -1
        if (version <= 0) {
            throw IllegalArgumentException("Missing backup version")
        }
        if (version != SETTINGS_BACKUP_VERSION) {
            throw UnsupportedOperationException("Unsupported backup version: $version")
        }

        val format = root.optString("format", "")
        if (
            format.isNotEmpty() &&
            format != SETTINGS_BACKUP_FORMAT &&
            format != PREVIOUS_SETTINGS_BACKUP_FORMAT &&
            format != LEGACY_SETTINGS_BACKUP_FORMAT
        ) {
            throw IllegalArgumentException("Invalid backup format")
        }

        val preferencesJson = root.optJSONObject("preferences")
            ?: throw IllegalArgumentException("Missing preferences payload")
        val widgetRestorePlanJson = root.optJSONObject("widgets")
            ?.takeIf { widgets -> (widgets.optJSONArray("slots")?.length() ?: 0) > 0 }
            ?.toString()
        val parsedPreferences = linkedMapOf<String, BackupPreference>()

        val preferenceKeys = mutableListOf<String>()
        val keys = preferencesJson.keys()
        while (keys.hasNext()) {
            preferenceKeys += keys.next()
        }

        preferenceKeys.sorted().forEach { key ->
            if (!isRestorablePreferenceKey(key)) {
                return@forEach
            }

            val entry = preferencesJson.optJSONObject(key)
                ?: throw IllegalArgumentException("Preference entry must be an object")
            val type = entry.optString("type")
            if (type.isBlank() || !entry.has("value")) {
                throw IllegalArgumentException("Preference entry is missing required fields")
            }

            parsedPreferences[key] = BackupPreference(
                type = type,
                value = parseBackupValue(type, entry.get("value"))
            )
        }

        return ParsedSettingsBackup(
            version = version,
            preferences = LinkedHashMap(parsedPreferences),
            widgetRestorePlanJson = widgetRestorePlanJson
        )
    }

    private fun applySettingsBackup(parsedBackup: ParsedSettingsBackup) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val hasWidgetRestorePlan = !parsedBackup.widgetRestorePlanJson.isNullOrBlank()
        val backupIncludesWidgetState = hasWidgetRestorePlan || parsedBackup.preferences.keys.any(::isWidgetPreferenceKey)

        prefs.all.keys
            .filter(::isRestorablePreferenceKey)
            .filter { backupIncludesWidgetState || !isWidgetPreferenceKey(it) }
            .forEach { editor.remove(it) }

        parsedBackup.preferences.forEach { (key, entry) ->
            if (hasWidgetRestorePlan && isWidgetPreferenceKey(key)) {
                return@forEach
            }
            when (entry.type) {
                "boolean" -> editor.putBoolean(key, entry.value as Boolean)
                "int" -> editor.putInt(key, entry.value as Int)
                "long" -> editor.putLong(key, entry.value as Long)
                "float" -> editor.putFloat(key, entry.value as Float)
                "string" -> editor.putString(key, entry.value as String)
                "string_set" -> {
                    val values = entry.value as? Set<*>
                        ?: throw IllegalArgumentException("Expected string set backup value")
                    editor.putStringSet(key, LinkedHashSet(values.filterIsInstance<String>()))
                }
                else -> throw IllegalArgumentException("Unsupported backup value type")
            }
        }

        if (hasWidgetRestorePlan) {
            editor.putString(MainActivity.PREF_PENDING_WIDGET_RESTORE, parsedBackup.widgetRestorePlanJson)
        } else {
            editor.remove(MainActivity.PREF_PENDING_WIDGET_RESTORE)
        }

        if (backupIncludesWidgetState) {
            editor.putBoolean(MainActivity.PREF_WIDGETS_DIRTY, true)
        }

        if (!editor.commit()) {
            throw IllegalStateException("Failed to commit restored settings")
        }
    }

    private fun parseBackupValue(type: String, rawValue: Any): Any {
        return when (type) {
            "boolean" -> rawValue as? Boolean
                ?: throw IllegalArgumentException("Expected boolean backup value")

            "int" -> parseWholeNumber(rawValue, Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()

            "long" -> parseWholeNumber(rawValue, Long.MIN_VALUE, Long.MAX_VALUE)

            "float" -> (rawValue as? Number)?.toFloat()
                ?: throw IllegalArgumentException("Expected float backup value")

            "string" -> rawValue as? String
                ?: throw IllegalArgumentException("Expected string backup value")

            "string_set" -> {
                val array = rawValue as? JSONArray
                    ?: throw IllegalArgumentException("Expected string array backup value")
                val values = linkedSetOf<String>()
                for (index in 0 until array.length()) {
                    val item = array.get(index) as? String
                        ?: throw IllegalArgumentException("String set contains a non-string value")
                    values += item
                }
                values
            }

            else -> throw IllegalArgumentException("Unsupported backup value type")
        }
    }

    private fun parseWholeNumber(rawValue: Any, min: Long, max: Long): Long {
        val number = rawValue as? Number
            ?: throw IllegalArgumentException("Expected numeric backup value")
        val doubleValue = number.toDouble()
        if (!doubleValue.isFinite()) {
            throw IllegalArgumentException("Backup number must be finite")
        }
        val longValue = number.toLong()
        if (doubleValue != longValue.toDouble() || longValue !in min..max) {
            throw IllegalArgumentException("Backup number must be a whole number in range")
        }
        return longValue
    }

    private fun promptShareSettingsBackup(uri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.backup_settings_share_title)
            .setMessage(R.string.backup_settings_share_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.backup_settings_share_action) { _, _ ->
                shareSettingsBackup(uri)
            }
            .show()
    }

    private fun shareSettingsBackup(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.backup_settings_share_chooser_title)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.backup_settings_share_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolveDisplayName(uri: Uri): String {
        requireContext().contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val name = cursor.getString(index)
                    if (!name.isNullOrBlank()) {
                        return name
                    }
                }
            }
        }
        return getString(R.string.restore_settings_selected_file)
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, selection: Int, onSelected: (Int) -> Unit) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(selection)
        var initialized = false
        spinner.post {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (!initialized) {
                        initialized = true
                        return
                    }
                    onSelected(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun isWidgetPreferenceKey(key: String): Boolean {
        return key == MainActivity.PREF_WIDGET_ORDER ||
            key.startsWith("widget_h_") ||
            key.startsWith("widget_fw_")
    }

    private fun isRestorablePreferenceKey(key: String): Boolean {
        return key != MainActivity.PREF_WIDGET_IDS_OLD &&
            key != MainActivity.PREF_PENDING_WIDGET_RESTORE &&
            key != MainActivity.PREF_WIDGETS_DIRTY
    }

    companion object {
        private const val   SETTINGS_BACKUP_FORMAT = "avox_settings"
        private const val PREVIOUS_SETTINGS_BACKUP_FORMAT = "a_launcher_settings"
        private const val LEGACY_SETTINGS_BACKUP_FORMAT = "launcher_settings"
        private const val SETTINGS_BACKUP_VERSION = 1
    }
}
