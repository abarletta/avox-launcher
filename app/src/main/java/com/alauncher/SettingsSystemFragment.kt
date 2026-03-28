package com.alauncher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class SettingsSystemFragment : Fragment() {

    private var screenMode: String = MODE_HOME

    private data class LaunchableAppEntry(
        val packageName: String,
        val label: String,
        val icon: Drawable?
    )

    private data class FavoriteRowStyle(
        val iconMode: String,
        val iconSizeDp: Int,
        val iconPackResolver: IconPackResolver?,
        val nerdTypeface: Typeface?
    )

    private val notifOptions = listOf(
        MainActivity.NOTIF_MODE_COUNT to "Badge Count",
        MainActivity.NOTIF_MODE_TEXT to "Notification Text",
        MainActivity.NOTIF_MODE_NONE to "Off"
    )

    private val blockCountOptions = listOf(
        2 to "2 (default)",
        3 to "3",
        4 to "4"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenMode = arguments?.getString(ARG_MODE, MODE_HOME) ?: MODE_HOME
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val notificationsSection = view.findViewById<View>(R.id.notificationsSection)
        val homeSection = view.findViewById<View>(R.id.homeSection)
        val widgetsSection = view.findViewById<View>(R.id.widgetsSection)
        val descriptionView = view.findViewById<TextView>(R.id.systemDescription)
        val titleView = view.findViewById<TextView>(R.id.systemTitle)
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        val showNotifications = screenMode == MODE_NOTIFICATIONS
        val showWidgets = screenMode == MODE_WIDGETS
        titleView.setText(
            when {
                showNotifications -> R.string.settings_notifications
                showWidgets -> R.string.settings_widgets
                else -> R.string.settings_home
            }
        )
        descriptionView.setText(
            when {
                showNotifications -> R.string.settings_notifications_hint
                showWidgets -> R.string.settings_widgets_hint
                else -> R.string.settings_home_hint
            }
        )
        notificationsSection.visibility = if (showNotifications) View.VISIBLE else View.GONE
        homeSection.visibility = if (!showNotifications && !showWidgets) View.VISIBLE else View.GONE
        widgetsSection.visibility = if (showWidgets) View.VISIBLE else View.GONE

        if (showNotifications) {
            setupSpinner(
                view.findViewById(R.id.notifModeSpinner), notifOptions.map { it.second },
                notifOptions.indexOfFirst { it.first == (prefs.getString(MainActivity.PREF_NOTIF_MODE, MainActivity.NOTIF_MODE_COUNT) ?: MainActivity.NOTIF_MODE_COUNT) }
                    .coerceAtLeast(0)
            ) { pos ->
                val mode = notifOptions[pos].first
                prefs.edit().putString(MainActivity.PREF_NOTIF_MODE, mode).apply()
                if (mode != MainActivity.NOTIF_MODE_NONE && !isNotificationListenerEnabled()) {
                    promptNotificationAccess()
                }
            }

            @Suppress("UseSwitchCompatOrMaterialCode")
            val swipeSwitch = view.findViewById<android.widget.Switch>(R.id.notifSwipeSwitch)
            swipeSwitch.isChecked = prefs.getBoolean(MainActivity.PREF_NOTIF_SWIPE, false)
            swipeSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(MainActivity.PREF_NOTIF_SWIPE, isChecked).apply()
            }
        }

        if (!showNotifications && !showWidgets) {
            view.findViewById<android.widget.Button>(R.id.favoritesButton).setOnClickListener {
                showAddFavoritePicker(view)
            }
            populateFavoritesList(view)

            setupSpinner(
                view.findViewById(R.id.footerNotifModeSpinner),
                notifOptions.map { it.second },
                notifOptions.indexOfFirst {
                    it.first == (prefs.getString(MainActivity.PREF_FOOTER_NOTIF_MODE, MainActivity.NOTIF_MODE_NONE)
                        ?: MainActivity.NOTIF_MODE_NONE)
                }.coerceAtLeast(0)
            ) { pos ->
                val mode = notifOptions[pos].first
                prefs.edit().putString(MainActivity.PREF_FOOTER_NOTIF_MODE, mode).apply()
                if (mode != MainActivity.NOTIF_MODE_NONE && !isNotificationListenerEnabled()) {
                    promptNotificationAccess()
                }
            }

            @Suppress("UseSwitchCompatOrMaterialCode")
            val footerLabelsSwitch = view.findViewById<android.widget.Switch>(R.id.footerLabelsSwitch)
            footerLabelsSwitch.isChecked = prefs.getBoolean(MainActivity.PREF_FOOTER_SHOW_LABELS, false)
            footerLabelsSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(MainActivity.PREF_FOOTER_SHOW_LABELS, isChecked).apply()
            }

            populateFooterActionRows(view)

            val requestedFooterSlot = requireActivity().intent.getIntExtra(SettingsActivity.EXTRA_FOOTER_SLOT_INDEX, -1)
            if (requestedFooterSlot in 0 until LauncherQuickActions.SLOT_COUNT) {
                requireActivity().intent.removeExtra(SettingsActivity.EXTRA_FOOTER_SLOT_INDEX)
                view.post { showFooterActionPicker(requestedFooterSlot, view) }
            }
        }

        if (showWidgets) {
            setupSpinner(
                view.findViewById(R.id.blockCountSpinner),
                blockCountOptions.map { it.second },
                blockCountOptions.indexOfFirst {
                    it.first == prefs.getInt(MainActivity.PREF_BLOCK_COUNT, MainActivity.DEFAULT_BLOCK_COUNT)
                }.coerceAtLeast(0)
            ) { pos ->
                prefs.edit().putInt(MainActivity.PREF_BLOCK_COUNT, blockCountOptions[pos].first).apply()
            }

            view.findViewById<android.widget.Button>(R.id.addWidgetButton).setOnClickListener {
                requireActivity().finish()
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("open_widget_picker", true)
                }
                startActivity(intent)
            }

            populateWidgetList(view)
        }
    }

    private fun populateFooterActionRows(rootView: View) {
        val container = rootView.findViewById<LinearLayout>(R.id.footerQuickActionsContainer)
        container.removeAllViews()

        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val density = resources.displayMetrics.density

        for (index in 0 until LauncherQuickActions.SLOT_COUNT) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
            }

            val title = TextView(requireContext()).apply {
                text = getString(R.string.footer_action_slot_label, index + 1)
                setTextColor(Color.WHITE)
                textSize = 14f
            }
            row.addView(title)

            val summary = TextView(requireContext()).apply {
                text = LauncherQuickActions.getDisplayLabel(requireContext(), prefs, index)
                setTextColor(Color.parseColor("#AAAAAA"))
                textSize = 13f
                setPadding(0, (4 * density).toInt(), 0, (8 * density).toInt())
            }
            row.addView(summary)

            val buttons = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val chooseButton = Button(requireContext()).apply {
                text = getString(R.string.footer_action_choose)
                setOnClickListener { showFooterActionPicker(index, rootView) }
            }
            buttons.addView(chooseButton)

            val clearButton = Button(requireContext()).apply {
                text = getString(R.string.footer_action_clear)
                isEnabled = LauncherQuickActions.getSpec(prefs, index) != null
                setOnClickListener {
                    LauncherQuickActions.setSpec(prefs, index, null)
                    populateFooterActionRows(rootView)
                }
            }
            buttons.addView(clearButton, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = (8 * density).toInt()
            })

            row.addView(buttons)
            container.addView(row)
        }
    }

    private fun showFooterActionPicker(slotIndex: Int, rootView: View) {
        val choices = LauncherQuickActions.getChoices(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.footer_action_pick_title)
            .setItems(choices.map { it.label }.toTypedArray()) { _, which ->
                val choice = choices[which]
                if (choice.spec == LauncherQuickActions.SPEC_PICK_APP) {
                    showFooterAppPicker(slotIndex, rootView)
                } else {
                    val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                    LauncherQuickActions.setSpec(prefs, slotIndex, choice.spec)
                    populateFooterActionRows(rootView)
                }
            }
            .show()
    }

    private fun showFooterAppPicker(slotIndex: Int, rootView: View) {
        val apps = LauncherQuickActions.loadLaunchableApps(requireContext())
        if (apps.isEmpty()) {
            Toast.makeText(requireContext(), R.string.launch_failed, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.footer_action_pick_app_title)
            .setItems(apps.map { it.second }.toTypedArray()) { _, which ->
                val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                LauncherQuickActions.setSpec(
                    prefs,
                    slotIndex,
                    LauncherQuickActions.buildAppSpec(apps[which].first)
                )
                populateFooterActionRows(rootView)
            }
            .show()
    }

    private fun populateFavoritesList(rootView: View) {
        val container = rootView.findViewById<LinearLayout>(R.id.favoritesContainer)
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoritePackages(prefs)
        val addButton = rootView.findViewById<Button>(R.id.favoritesButton)
        addButton.isEnabled = favorites.size < MAX_FAVORITES
        addButton.alpha = if (addButton.isEnabled) 1f else 0.5f

        container.removeAllViews()

        val appsByPackage = loadLaunchableAppsWithIcons().associateBy { it.packageName }
        val rowStyle = getFavoriteRowStyle(prefs)
        val density = resources.displayMetrics.density

        favorites.forEachIndexed { index, packageName ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val appRow = layoutInflater.inflate(R.layout.item_app, row, false)
            bindLauncherRow(appRow, appsByPackage[packageName], packageName, rowStyle)
            row.addView(
                appRow,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )

            val actions = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            actions.addView(
                createMoveButton(
                    label = "▲",
                    enabled = index > 0,
                    description = getString(R.string.move_up_label)
                ) {
                    swapFavorites(index, index - 1, rootView)
                }
            )
            actions.addView(
                createMoveButton(
                    label = "▼",
                    enabled = index < favorites.lastIndex,
                    description = getString(R.string.move_down_label)
                ) {
                    swapFavorites(index, index + 1, rootView)
                }
            )
            actions.addView(
                createIconActionButton(
                    iconRes = R.drawable.ic_delete,
                    contentDescription = getString(R.string.remove_favorite_label),
                    tint = Color.parseColor("#FF6666"),
                    enabled = favorites.size > MIN_FAVORITES
                ) {
                    removeFavorite(packageName, rootView)
                }
            )

            row.addView(actions)
            container.addView(row)

            if (index < favorites.lastIndex) {
                container.addView(View(requireContext()).apply {
                    setBackgroundColor(Color.parseColor("#22FFFFFF"))
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    topMargin = (4 * density).toInt()
                    bottomMargin = (4 * density).toInt()
                })
            }
        }
    }

    private fun showAddFavoritePicker(rootView: View) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoritePackages(prefs)
        if (favorites.size >= MAX_FAVORITES) {
            Toast.makeText(requireContext(), R.string.favorites_min_max, Toast.LENGTH_SHORT).show()
            return
        }

        val availableApps = loadLaunchableAppsWithIcons()
            .filterNot { favorites.contains(it.packageName) }

        if (availableApps.isEmpty()) {
            Toast.makeText(requireContext(), R.string.favorites_picker_empty, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.favorites_add_label)
            .setAdapter(FavoritesPickerAdapter(availableApps, getFavoriteRowStyle(prefs))) { _, which ->
                val updated = favorites.toMutableList().apply { add(availableApps[which].packageName) }
                saveFavoritePackages(prefs, updated)
                populateFavoritesList(rootView)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun bindLauncherRow(
        view: View,
        app: LaunchableAppEntry?,
        packageName: String,
        rowStyle: FavoriteRowStyle
    ) {
        val iconView = view.findViewById<ImageView>(R.id.appIcon)
        val badgeView = view.findViewById<TextView>(R.id.notifBadge)
        val nameView = view.findViewById<TextView>(R.id.appName)
        val detailView = view.findViewById<TextView>(R.id.notificationText)
        val iconFrame = iconView.parent as? FrameLayout
        val density = view.resources.displayMetrics.density
        val iconSizePx = (rowStyle.iconSizeDp * density).toInt()
        val showRegularIcon = rowStyle.iconMode == MainActivity.ICON_MODE_REGULAR
        val displayIcon = if (showRegularIcon) {
            rowStyle.iconPackResolver?.resolve(packageName)
                ?: app?.icon
                ?: requireContext().packageManager.defaultActivityIcon
        } else {
            null
        }

        badgeView.visibility = View.GONE
        nameView.textSize = 16f
        detailView.textSize = 12f

        if (showRegularIcon) {
            iconView.setImageDrawable(displayIcon)
            iconView.visibility = View.VISIBLE
            iconFrame?.layoutParams?.width = iconSizePx
            iconFrame?.layoutParams?.height = iconSizePx
            iconView.layoutParams?.width = iconSizePx
            iconView.layoutParams?.height = iconSizePx
            iconFrame?.visibility = View.VISIBLE
        } else {
            iconView.setImageDrawable(null)
            iconView.visibility = View.GONE
            iconFrame?.layoutParams?.width = 0
            iconFrame?.layoutParams?.height = 0
            iconFrame?.visibility = View.GONE
        }

        if (app != null) {
            val nerdTypeface = rowStyle.nerdTypeface
            val glyph = if (rowStyle.iconMode == MainActivity.ICON_MODE_NERD && nerdTypeface != null) {
                launcherNerdGlyphs[packageName]
            } else {
                null
            }
            if (glyph != null) {
                val separator = "\u2003"
                val spannable = android.text.SpannableString("$glyph$separator${app.label}")
                spannable.setSpan(
                    NerdFontSpan(nerdTypeface!!),
                    0,
                    glyph.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val glyphScale = rowStyle.iconSizeDp.toFloat() / nameView.textSize.coerceAtLeast(1f)
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(glyphScale.coerceIn(0.5f, 3f)),
                    0,
                    glyph.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                nameView.text = spannable
            } else {
                nameView.text = app.label
            }
            detailView.visibility = View.GONE
        } else {
            nameView.text = getString(R.string.footer_action_unavailable)
            detailView.text = packageName
            detailView.visibility = View.VISIBLE
        }
    }

    private fun getFavoriteRowStyle(prefs: android.content.SharedPreferences): FavoriteRowStyle {
        val iconMode = prefs.getString(MainActivity.PREF_ICON_MODE, null)
            ?: if (prefs.getBoolean(MainActivity.PREF_NERD_FONT, false)) MainActivity.ICON_MODE_NERD else MainActivity.ICON_MODE_REGULAR
        val iconPack = prefs.getString(MainActivity.PREF_ICON_PACK, "") ?: ""
        val iconPackResolver = if (iconMode == MainActivity.ICON_MODE_REGULAR && iconPack.isNotBlank()) {
            IconPackResolver(requireContext()).apply { load(iconPack) }
        } else {
            null
        }
        val nerdTypeface = if (iconMode == MainActivity.ICON_MODE_NERD) {
            val nerdFile = java.io.File(requireContext().filesDir, "nerd_font.ttf")
            if (nerdFile.exists()) {
                try {
                    Typeface.createFromFile(nerdFile)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
        return FavoriteRowStyle(
            iconMode = iconMode,
            iconSizeDp = prefs.getInt(MainActivity.PREF_ICON_SIZE, MainActivity.DEFAULT_ICON_SIZE),
            iconPackResolver = iconPackResolver,
            nerdTypeface = nerdTypeface
        )
    }

    private fun createMoveButton(
        label: String,
        enabled: Boolean,
        description: String,
        onClick: () -> Unit
    ): TextView {
        val density = resources.displayMetrics.density
        return TextView(requireContext()).apply {
            text = label
            contentDescription = description
            setTextColor(if (enabled) Color.WHITE else Color.parseColor("#444444"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (4 * density).toInt())
            if (enabled) {
                setOnClickListener { onClick() }
            }
        }
    }

    private fun createIconActionButton(
        iconRes: Int,
        contentDescription: String,
        tint: Int = Color.WHITE,
        enabled: Boolean = true,
        onClick: () -> Unit
    ): ImageButton {
        val density = resources.displayMetrics.density
        return ImageButton(requireContext()).apply {
            setImageResource(iconRes)
            background = null
            imageTintList = ColorStateList.valueOf(tint)
            this.contentDescription = contentDescription
            setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.35f
            if (enabled) {
                setOnClickListener { onClick() }
            }
        }
    }

    private fun getFavoritePackages(prefs: android.content.SharedPreferences): MutableList<String> {
        val saved = prefs.getString(MainActivity.PREF_FAVORITES, null)
        if (!saved.isNullOrBlank()) {
            val parsed = saved.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
            if (parsed.isNotEmpty()) {
                return parsed.toMutableList()
            }
        }
        return MainActivity.DEFAULT_FAVORITES.toMutableList()
    }

    private fun saveFavoritePackages(
        prefs: android.content.SharedPreferences,
        favoritePackages: List<String>
    ) {
        prefs.edit().putString(
            MainActivity.PREF_FAVORITES,
            favoritePackages.distinct().take(MAX_FAVORITES).joinToString(",")
        ).apply()
    }

    private fun swapFavorites(from: Int, to: Int, rootView: View) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoritePackages(prefs)
        if (from !in favorites.indices || to !in favorites.indices) return
        val moved = favorites.removeAt(from)
        favorites.add(to, moved)
        saveFavoritePackages(prefs, favorites)
        populateFavoritesList(rootView)
    }

    private fun removeFavorite(packageName: String, rootView: View) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoritePackages(prefs)
        if (favorites.size <= MIN_FAVORITES) {
            Toast.makeText(requireContext(), R.string.favorites_min_max, Toast.LENGTH_SHORT).show()
            return
        }
        favorites.remove(packageName)
        saveFavoritePackages(prefs, favorites)
        populateFavoritesList(rootView)
    }

    private fun loadLaunchableAppsWithIcons(): List<LaunchableAppEntry> {
        val pm = requireContext().packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        return pm.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                LaunchableAppEntry(
                    packageName = activityInfo.packageName,
                    label = resolveInfo.loadLabel(pm)?.toString().orEmpty().ifBlank { activityInfo.packageName },
                    icon = try {
                        resolveInfo.loadIcon(pm)
                    } catch (_: Exception) {
                        null
                    }
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun populateWidgetList(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.widgetManageContainer)
        container.removeAllViews()
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val orderStr = prefs.getString(MainActivity.PREF_WIDGET_ORDER, null)
        if (orderStr.isNullOrBlank()) {
            val hint = TextView(requireContext()).apply {
                text = getString(R.string.widget_none_label)
                setTextColor(Color.parseColor("#888888"))
                textSize = 14f
            }
            container.addView(hint)
            return
        }
        val ids = orderStr.split(",").mapNotNull { it.toIntOrNull() }
        if (ids.isEmpty()) return
        val awm = AppWidgetManager.getInstance(requireContext())
        val density = resources.displayMetrics.density

        for ((index, widgetId) in ids.withIndex()) {
            val info = awm.getAppWidgetInfo(widgetId) ?: continue
            val label = info.loadLabel(requireContext().packageManager) ?: "Widget"

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
            }

            val header = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Move up/down buttons
            val moveUpBtn = createMoveButton(
                label = "▲",
                enabled = index > 0,
                description = getString(R.string.move_up_label)
            ) {
                swapWidgets(ids, index, index - 1, view)
            }
            header.addView(moveUpBtn)
            val moveDownBtn = createMoveButton(
                label = "▼",
                enabled = index < ids.size - 1,
                description = getString(R.string.move_down_label)
            ) {
                swapWidgets(ids, index, index + 1, view)
            }
            header.addView(moveDownBtn)

            val nameView = TextView(requireContext()).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 15f
            }
            header.addView(nameView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            if (info.configure != null) {
                header.addView(
                    createIconActionButton(
                        iconRes = R.drawable.ic_settings,
                        contentDescription = getString(R.string.widget_controls_label)
                    ) {
                        openWidgetControls(widgetId)
                    }
                )
            }
            header.addView(
                createIconActionButton(
                    iconRes = R.drawable.ic_delete,
                    contentDescription = getString(R.string.widget_remove_label),
                    tint = Color.parseColor("#FF6666")
                ) {
                    removeWidgetFromSettings(widgetId, view)
                }
            )
            row.addView(header)

            val controlsRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, (4 * density).toInt(), 0, 0)
            }

            val fullWidthSwitch = Switch(requireContext()).apply {
                text = getString(R.string.widget_full_width_label)
                isChecked = prefs.getBoolean("widget_fw_$widgetId", false)
                setTextColor(Color.WHITE)
                setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit()
                        .putBoolean("widget_fw_$widgetId", isChecked)
                        .putBoolean(MainActivity.PREF_WIDGETS_DIRTY, true)
                        .apply()
                }
            }
            controlsRow.addView(fullWidthSwitch, LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ))
            row.addView(controlsRow)

            val heightRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, (4 * density).toInt(), 0, 0)
            }
            val heightLabel = TextView(requireContext()).apply {
                text = getString(R.string.widget_height_label)
                setTextColor(Color.parseColor("#AAAAAA"))
                textSize = 13f
            }
            heightRow.addView(heightLabel, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = (8 * density).toInt() })

            val savedHeightPx = prefs.getInt("widget_h_$widgetId", -1)
            val currentDp = if (savedHeightPx > 0) (savedHeightPx / density).toInt()
                            else info.minHeight.coerceAtLeast(MainActivity.MIN_WIDGET_HEIGHT_DP)
            val heightValue = TextView(requireContext()).apply {
                text = "${currentDp}dp"
                setTextColor(Color.WHITE)
                textSize = 13f
                minWidth = (40 * density).toInt()
                gravity = Gravity.END
            }
            val slider = SeekBar(requireContext()).apply {
                max = MainActivity.MAX_WIDGET_HEIGHT_DP - MainActivity.MIN_WIDGET_HEIGHT_DP
                progress = (currentDp - MainActivity.MIN_WIDGET_HEIGHT_DP).coerceIn(0, max)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        val dpVal = progress + MainActivity.MIN_WIDGET_HEIGHT_DP
                        heightValue.text = "${dpVal}dp"
                        val px = (dpVal * density).toInt()
                        prefs.edit()
                            .putInt("widget_h_$widgetId", px)
                            .putBoolean(MainActivity.PREF_WIDGETS_DIRTY, true)
                            .apply()
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            heightRow.addView(slider, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            heightRow.addView(heightValue)
            row.addView(heightRow)

            container.addView(row)
        }
    }

    private fun swapWidgets(ids: List<Int>, from: Int, to: Int, rootView: View) {
        val mutable = ids.toMutableList()
        val temp = mutable[from]
        mutable[from] = mutable[to]
        mutable[to] = temp
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(MainActivity.PREF_WIDGET_ORDER, mutable.joinToString(","))
            .putBoolean(MainActivity.PREF_WIDGETS_DIRTY, true)
            .apply()
        populateWidgetList(rootView)
    }

    private fun removeWidgetFromSettings(widgetId: Int, rootView: View) {
        val prefs = requireContext().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val host = AppWidgetHost(requireContext(), MainActivity.APPWIDGET_HOST_ID)
        try { host.deleteAppWidgetId(widgetId) } catch (_: Exception) {}
        val order = prefs.getString(MainActivity.PREF_WIDGET_ORDER, "") ?: ""
        val newIds = order.split(",").mapNotNull { it.toIntOrNull() }.filter { it != widgetId }
        prefs.edit()
            .putString(MainActivity.PREF_WIDGET_ORDER, if (newIds.isEmpty()) "" else newIds.joinToString(","))
            .remove("widget_h_$widgetId")
            .putBoolean(MainActivity.PREF_WIDGETS_DIRTY, true)
            .apply()
        populateWidgetList(rootView)
    }

    private fun openWidgetControls(widgetId: Int) {
        val awm = AppWidgetManager.getInstance(requireContext())
        val info = awm.getAppWidgetInfo(widgetId)
        val configureComponent = info?.configure
        if (configureComponent == null) {
            Toast.makeText(requireContext(), R.string.widget_controls_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = configureComponent
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            })
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.widget_controls_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(requireContext().contentResolver, "enabled_notification_listeners") ?: return false
        val component = ComponentName(requireContext(), NotificationService::class.java)
        return flat.contains(component.flattenToString())
    }

    private fun promptNotificationAccess() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.notification_access_title)
            .setMessage(R.string.notification_access_message)
            .setPositiveButton(R.string.grant) { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
                    if (!initialized) { initialized = true; return }
                    onSelected(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private inner class FavoritesPickerAdapter(
        private val apps: List<LaunchableAppEntry>,
        private val rowStyle: FavoriteRowStyle
    ) : BaseAdapter() {

        override fun getCount(): Int = apps.size

        override fun getItem(position: Int): Any = apps[position]

        override fun getItemId(position: Int): Long = apps[position].packageName.hashCode().toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_app, parent, false)
            bindLauncherRow(view, apps[position], apps[position].packageName, rowStyle)
            return view
        }
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val MIN_FAVORITES = 3
        private const val MAX_FAVORITES = 10
        const val MODE_NOTIFICATIONS = "notifications"
        const val MODE_HOME = "home"
        const val MODE_WIDGETS = "widgets"
        const val MODE_WIDGETS_HOME = MODE_HOME

        fun newInstance(mode: String): SettingsSystemFragment {
            return SettingsSystemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MODE, mode)
                }
            }
        }
    }
}
