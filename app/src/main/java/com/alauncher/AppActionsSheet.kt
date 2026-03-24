package com.alauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class AppActionsSheet(
    private val context: Context,
    private val packageName: String,
    private val appLabel: String
) {
    fun show() {
        val dialog = BottomSheetDialog(context, R.style.Theme_ALauncher_BottomSheet)
        val view = LayoutInflater.from(context).inflate(R.layout.sheet_app_actions, null)
        dialog.setContentView(view)

        val titleView = view.findViewById<TextView>(R.id.sheetAppName)
        titleView.text = appLabel

        try {
            val icon = context.packageManager.getApplicationIcon(packageName)
            titleView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            titleView.compoundDrawablePadding = (12 * context.resources.displayMetrics.density).toInt()
        } catch (_: Exception) { }

        // App info
        view.findViewById<View>(R.id.actionAppInfo).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            dialog.dismiss()
        }

        // Play Store
        view.findViewById<View>(R.id.actionPlayStore).apply {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            if (storeIntent.resolveActivity(context.packageManager) != null) {
                setOnClickListener {
                    context.startActivity(storeIntent)
                    dialog.dismiss()
                }
            } else {
                visibility = View.GONE
            }
        }

        // Uninstall
        view.findViewById<View>(R.id.actionUninstall).apply {
            val uninstallIntent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
            setOnClickListener {
                try {
                    context.startActivity(uninstallIntent)
                } catch (_: Exception) {
                    Toast.makeText(context, R.string.action_uninstall_failed, Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        // Shortcuts
        val shortcutsContainer = view.findViewById<LinearLayout>(R.id.shortcutsContainer)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            try {
                val query = LauncherApps.ShortcutQuery().apply {
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    )
                    setPackage(packageName)
                }
                val shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle())
                shortcuts?.forEach { shortcut ->
                    addShortcutRow(shortcutsContainer, launcherApps, shortcut, dialog)
                }
            } catch (_: Exception) { }
        }

        dialog.show()
    }

    private fun addShortcutRow(
        container: LinearLayout,
        launcherApps: LauncherApps,
        shortcut: ShortcutInfo,
        dialog: BottomSheetDialog
    ) {
        val row = LayoutInflater.from(context).inflate(R.layout.item_shortcut, container, false)
        val label = row.findViewById<TextView>(R.id.shortcutLabel)
        val icon = row.findViewById<ImageView>(R.id.shortcutIcon)

        label.text = shortcut.shortLabel ?: shortcut.id

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                val drawable = launcherApps.getShortcutIconDrawable(shortcut, context.resources.displayMetrics.densityDpi)
                if (drawable != null) {
                    icon.setImageDrawable(drawable)
                } else {
                    icon.setImageResource(R.drawable.ic_shortcut)
                }
            } catch (_: Exception) {
                icon.setImageResource(R.drawable.ic_shortcut)
            }
        }

        row.setOnClickListener {
            try {
                launcherApps.startShortcut(shortcut, null, null)
            } catch (_: Exception) {
                Toast.makeText(context, R.string.launch_failed, Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        container.addView(row)
    }
}
