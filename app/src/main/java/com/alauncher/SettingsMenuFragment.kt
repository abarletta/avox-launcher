package com.alauncher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject

class SettingsMenuFragment : Fragment() {

    private data class BackupPreference(
        val type: String,
        val value: Any
    )

    private data class ParsedSettingsBackup(
        val version: Int,
        val preferences: LinkedHashMap<String, BackupPreference>
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
            requireContext().contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
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
            Toast.makeText(requireContext(), R.string.restore_settings_success, Toast.LENGTH_SHORT).show()
            requireActivity().recreate()
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
    }

    private fun loadSettingsBackup(uri: Uri): ParsedSettingsBackup {
        val content = requireContext().contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
            it.readText()
        } ?: error("Unable to open restore source")
        return parseSettingsBackup(JSONObject(content))
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
        if (format.isNotEmpty() && format != SETTINGS_BACKUP_FORMAT && format != LEGACY_SETTINGS_BACKUP_FORMAT) {
            throw IllegalArgumentException("Invalid backup format")
        }

        val preferencesJson = root.optJSONObject("preferences")
            ?: throw IllegalArgumentException("Missing preferences payload")
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
            preferences = LinkedHashMap(parsedPreferences)
        )
    }

    private fun applySettingsBackup(parsedBackup: ParsedSettingsBackup) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        prefs.all.keys
            .filter(::isRestorablePreferenceKey)
            .forEach { editor.remove(it) }

        parsedBackup.preferences.forEach { (key, entry) ->
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

    private fun isRestorablePreferenceKey(key: String): Boolean {
        return key != MainActivity.PREF_WIDGET_ORDER &&
            key != MainActivity.PREF_WIDGET_IDS_OLD &&
            key != MainActivity.PREF_WIDGETS_DIRTY &&
            !key.startsWith("widget_h_") &&
            !key.startsWith("widget_fw_")
    }

    companion object {
        private const val SETTINGS_BACKUP_FORMAT = "a_launcher_settings"
        private const val LEGACY_SETTINGS_BACKUP_FORMAT = "launcher_settings"
        private const val SETTINGS_BACKUP_VERSION = 1
    }
}
