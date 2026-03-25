package com.alauncher

import android.app.SearchManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var allApps = listOf<AppInfo>()
    private val displayedApps = mutableListOf<AppInfo>()
    private var isExpandedView = false
    private var notificationData = mapOf<String, NotifInfo>()

    private lateinit var appList: ListView
    private lateinit var sidebar: AlphabetSidebar
    private lateinit var darkOverlay: View
    private lateinit var bottomButton: ImageButton
    private lateinit var searchBar: EditText
    private lateinit var widgetContainer: LinearLayout

    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager

    // Widget edit mode
    private var isWidgetEditMode = false
    private var pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var pendingProvider: ComponentName? = null
    private var activeWidgetIds = mutableListOf<Int>()

    // Widget state
    private var widgetsRestored = false
    private var widgetsDirty = false
    private var pendingFinalizeWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // Widget resize drag state
    private var resizingWrapper: View? = null
    private var resizeStartY = 0f
    private var resizeStartHeight = 0

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshNotificationData()
            refreshList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appList = findViewById(R.id.appList)
        sidebar = findViewById(R.id.alphabetSidebar)
        darkOverlay = findViewById(R.id.darkOverlay)
        bottomButton = findViewById(R.id.bottomButton)
        searchBar = findViewById(R.id.searchBar)
        widgetContainer = findViewById(R.id.widgetContainer)

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)

        bottomButton.setOnClickListener {
            if (isWidgetEditMode) {
                exitWidgetEditMode()
            } else if (isExpandedView) {
                showSearchBar()
            } else {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        bottomButton.setOnLongClickListener {
            if (isExpandedView) {
                openSystemSearch()
            } else {
                selectWidget()
            }
            true
        }

        sidebar.onLetterSelected = { letter ->
            hideSearchBar()
            if (isWidgetEditMode) exitWidgetEditMode()
            when (letter) {
                SIDEBAR_FAVORITES -> {
                    isExpandedView = false
                    showFavorites()
                    updateBottomButton()
                }
                SIDEBAR_OTHER -> {
                    isExpandedView = true
                    showNonAlphaApps()
                    updateBottomButton()
                }
                else -> {
                    isExpandedView = true
                    showAppsForLetter(letter)
                    updateBottomButton()
                }
            }
        }

        appList.setOnItemClickListener { _, _, position, _ ->
            val app = (appList.adapter as? AppListAdapter)?.getAppInfo(position) ?: return@setOnItemClickListener
            try {
                startActivity(app.launchIntent)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
            }
        }

        appList.setOnItemLongClickListener { _, _, position, _ ->
            val app = (appList.adapter as? AppListAdapter)?.getAppInfo(position)
            if (app != null) {
                AppActionsSheet(this, app.packageName, app.label).show()
            }
            true
        }

        // Swipe gestures for notifications
        val swipeDetector = android.view.GestureDetector(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val swipePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                if (!swipePrefs.getBoolean(PREF_NOTIF_SWIPE, false)) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (kotlin.math.abs(dx) > kotlin.math.abs(dy) && kotlin.math.abs(dx) > 100) {
                    val position = appList.pointToPosition(e1.x.toInt(), e1.y.toInt())
                    val app = (appList.adapter as? AppListAdapter)?.getAppInfo(position)
                    if (position >= 0 && app != null) {
                        if (dx < 0) {
                            NotificationHolder.service?.cancelNotificationsForPackage(app.packageName)
                            refreshNotificationData()
                            refreshList()
                        } else {
                            AppActionsSheet(this@MainActivity, app.packageName, app.label).show()
                        }
                        return true
                    }
                }
                return false
            }
        })
        appList.setOnTouchListener { _, event ->
            swipeDetector.onTouchEvent(event)
            false
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString().orEmpty())
            }
        })

        registerReceiver(
            notificationReceiver,
            IntentFilter(NotificationService.ACTION_NOTIFICATION_UPDATE),
            RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onResume() {
        super.onResume()
        allApps = loadLaunchableApps()
        refreshNotificationData()
        if (!isExpandedView) showFavorites()
        setupSidebar()
        applySettings()
        applyLayoutPrefs()
        updateBottomButton()

        val prefs2 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs2.getBoolean(PREF_WIDGETS_DIRTY, false)) {
            prefs2.edit().remove(PREF_WIDGETS_DIRTY).apply()
            widgetsRestored = false
        }
        if (!widgetsRestored) {
            restoreWidgets()
            widgetsRestored = true
        } else {
            refreshWidgetSizes()
        }

        // Handle widget picker trigger from settings
        if (intent.getBooleanExtra("open_widget_picker", false)) {
            intent.removeExtra("open_widget_picker")
            widgetContainer.post { selectWidget() }
        }

        // Finalize any widget deferred from onActivityResult
        if (pendingFinalizeWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val wId = pendingFinalizeWidgetId
            pendingFinalizeWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            finalizeWidget(wId)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { appWidgetHost.stopListening() } catch (_: Exception) {}
        try { unregisterReceiver(notificationReceiver) } catch (_: Exception) {}
    }

    @Deprecated("Use OnBackPressedCallback", ReplaceWith("onBackPressedDispatcher"))
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (isWidgetEditMode) {
            exitWidgetEditMode()
            return
        }
        if (searchBar.visibility == View.VISIBLE) {
            hideSearchBar()
            return
        }
        if (isExpandedView) {
            isExpandedView = false
            showFavorites()
            updateBottomButton()
            return
        }
    }

    @Deprecated("Use Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val resultWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId)
            ?: pendingWidgetId

        when (requestCode) {
            REQUEST_BIND_WIDGET -> {
                if (resultCode == RESULT_OK && resultWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // After bind permission granted, check if widget needs configuration
                    val info = appWidgetManager.getAppWidgetInfo(resultWidgetId)
                    if (info != null && info.configure != null) {
                        pendingWidgetId = resultWidgetId
                        @Suppress("DEPRECATION")
                        startActivityForResult(
                            Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                                component = info.configure
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, resultWidgetId)
                            },
                            REQUEST_CONFIGURE_WIDGET
                        )
                    } else {
                        // Defer finalization to onResume where the host is fully active
                        pendingFinalizeWidgetId = resultWidgetId
                    }
                } else if (resultWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    try { appWidgetHost.deleteAppWidgetId(resultWidgetId) } catch (_: Exception) {}
                    pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
                    pendingProvider = null
                }
            }
            REQUEST_CONFIGURE_WIDGET -> {
                if (resultCode == RESULT_OK && resultWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // Defer finalization to onResume where the host is fully active
                    pendingFinalizeWidgetId = resultWidgetId
                } else if (resultWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    try { appWidgetHost.deleteAppWidgetId(resultWidgetId) } catch (_: Exception) {}
                }
                pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
                pendingProvider = null
            }
        }
    }

    // --- View state ---

    private fun getFavoritePackages(): Set<String> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val saved = prefs.getString(PREF_FAVORITES, null)
        if (saved != null && saved.isNotBlank()) {
            return saved.split(",").toSet()
        }
        return DEFAULT_FAVORITES
    }

    private fun showFavorites() {
        val favorites = getFavoritePackages()
        displayedApps.clear()
        displayedApps.addAll(allApps.filter { favorites.contains(it.packageName) })
        animateListTransition()
    }

    private fun showAppsForLetter(letter: String) {
        displayedApps.clear()
        displayedApps.addAll(allApps.filter {
            it.label.startsWith(letter, ignoreCase = true)
        })
        animateListTransition()
    }

    private fun showNonAlphaApps() {
        displayedApps.clear()
        displayedApps.addAll(allApps.filter {
            val first = it.label.firstOrNull()
            first != null && !first.isLetter()
        })
        animateListTransition()
    }

    private fun animateListTransition() {
        appList.animate().alpha(0f).setDuration(120).withEndAction {
            refreshList()
            appList.animate().alpha(1f).setDuration(180).start()
        }.start()
    }

    private fun filterApps(query: String) {
        displayedApps.clear()
        if (query.isBlank()) {
            if (isExpandedView) {
                displayedApps.addAll(allApps)
            } else {
                showFavorites()
                return
            }
        } else {
            displayedApps.addAll(allApps.filter {
                it.label.contains(query, ignoreCase = true)
            })
        }
        refreshList()
    }

    private var iconPackResolver: IconPackResolver? = null

    private fun refreshList() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val fontFamily = prefs.getString(PREF_FONT, DEFAULT_FONT) ?: DEFAULT_FONT
        val spacing = prefs.getInt(PREF_SPACING, DEFAULT_SPACING)
        val fontSize = prefs.getInt(PREF_FONT_SIZE, DEFAULT_FONT_SIZE)
        val notifMode = prefs.getString(PREF_NOTIF_MODE, NOTIF_MODE_COUNT) ?: NOTIF_MODE_COUNT
        val alignment = prefs.getString(PREF_ALIGNMENT, "left") ?: "left"
        val iconSize = prefs.getInt(PREF_ICON_SIZE, DEFAULT_ICON_SIZE)
        val typeface = resolveTypeface(fontFamily)

        // Icon pack
        val iconPack = prefs.getString(PREF_ICON_PACK, "") ?: ""
        if (iconPack.isNotEmpty()) {
            if (iconPackResolver == null || iconPackResolver?.let { true } == true) {
                iconPackResolver = IconPackResolver(this)
                iconPackResolver?.load(iconPack)
            }
        } else {
            iconPackResolver = null
        }

        // Nerd font / icon mode
        val iconMode = prefs.getString(PREF_ICON_MODE, null)
            ?: if (prefs.getBoolean(PREF_NERD_FONT, false)) ICON_MODE_NERD else ICON_MODE_REGULAR
        val nerdTypeface = if (iconMode == ICON_MODE_NERD) {
            val nerdFile = java.io.File(filesDir, "nerd_font.ttf")
            if (nerdFile.exists()) try { Typeface.createFromFile(nerdFile) } catch (_: Exception) { null }
            else null
        } else null

        appList.adapter = AppListAdapter(
            layoutInflater, displayedApps, typeface, spacing, fontSize,
            notificationData, notifMode, alignment == "center",
            iconSize, iconPackResolver, nerdTypeface, iconMode,
            showHeaders = isExpandedView
        )
    }

    private fun updateBottomButton() {
        if (isExpandedView) {
            bottomButton.setImageResource(R.drawable.ic_search)
            bottomButton.contentDescription = getString(R.string.search_button_label)
            if (widgetContainer.visibility == View.VISIBLE) {
                widgetContainer.animate().alpha(0f).setDuration(150).withEndAction {
                    widgetContainer.visibility = View.GONE
                }.start()
            }
        } else {
            bottomButton.setImageResource(R.drawable.ic_settings)
            bottomButton.contentDescription = getString(R.string.settings_label)
            if (widgetContainer.visibility != View.VISIBLE) {
                widgetContainer.alpha = 0f
                widgetContainer.visibility = View.VISIBLE
                widgetContainer.animate().alpha(1f).setDuration(150).start()
            }
        }
        updateListPaddingForWidgets()
    }

    private fun showSearchBar() {
        searchBar.visibility = View.VISIBLE
        searchBar.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSearchBar() {
        searchBar.visibility = View.GONE
        searchBar.text.clear()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchBar.windowToken, 0)
    }

    private fun setupSidebar() {
        val items = mutableListOf(SIDEBAR_FAVORITES)
        val usedLetters = allApps
            .mapNotNull { it.label.firstOrNull()?.uppercaseChar() }
            .filter { it.isLetter() }
            .distinct()
            .sorted()
            .map { it.toString() }
        items.addAll(usedLetters)

        val hasNonAlpha = allApps.any {
            val first = it.label.firstOrNull()
            first != null && !first.isLetter()
        }
        if (hasNonAlpha) items.add(SIDEBAR_OTHER)

        sidebar.setItems(items)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val fontFamily = prefs.getString(PREF_FONT, DEFAULT_FONT) ?: DEFAULT_FONT
        sidebar.setFontFamily(fontFamily)

        val animStyle = prefs.getString(PREF_ANIM_STYLE, AlphabetSidebar.STYLE_WAVE) ?: AlphabetSidebar.STYLE_WAVE
        sidebar.setAnimationStyle(animStyle)
        sidebar.setWaveShift(prefs.getInt(PREF_WAVE_SHIFT, DEFAULT_WAVE_SHIFT).toFloat())
        sidebar.setWaveScale(1f + prefs.getInt(PREF_WAVE_SCALE, DEFAULT_WAVE_SCALE) / 10f)
        sidebar.setHighlightIntensity(prefs.getInt(PREF_HIGHLIGHT_INTENSITY, DEFAULT_HIGHLIGHT_INTENSITY) / 10f)
        sidebar.setFadeRadius(prefs.getInt(PREF_FADE_RADIUS, DEFAULT_FADE_RADIUS) / 10f)
    }

    private fun applySettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val effect = prefs.getString(PREF_WALLPAPER_EFFECT, WALLPAPER_EFFECT_DARKEN) ?: WALLPAPER_EFFECT_DARKEN
        val darkness = prefs.getInt(PREF_DARKNESS, DEFAULT_DARKNESS) / 100f

        when (effect) {
            WALLPAPER_EFFECT_BLUR -> {
                val radius = prefs.getInt(PREF_BLUR_RADIUS, DEFAULT_BLUR_RADIUS)
                val blurred = getBlurredWallpaper(radius)
                if (blurred != null) {
                    darkOverlay.background = android.graphics.drawable.BitmapDrawable(resources, blurred)
                    darkOverlay.alpha = 1f
                } else {
                    darkOverlay.setBackgroundColor(Color.BLACK)
                    darkOverlay.alpha = darkness
                }
            }
            WALLPAPER_EFFECT_COLOR -> {
                val tintColor = prefs.getString(PREF_COLOR_TINT, DEFAULT_COLOR_TINT) ?: DEFAULT_COLOR_TINT
                try {
                    darkOverlay.setBackgroundColor(Color.parseColor(tintColor))
                } catch (_: Exception) {
                    darkOverlay.setBackgroundColor(Color.BLACK)
                }
                darkOverlay.alpha = darkness
            }
            else -> {
                darkOverlay.setBackgroundColor(Color.BLACK)
                darkOverlay.alpha = darkness
            }
        }
    }

    private fun getBlurredWallpaper(radius: Int): android.graphics.Bitmap? {
        return try {
            val wm = android.app.WallpaperManager.getInstance(this)
            val wallpaper = wm.drawable ?: return null
            val origW = wallpaper.intrinsicWidth.coerceAtLeast(1)
            val origH = wallpaper.intrinsicHeight.coerceAtLeast(1)
            val origBitmap = android.graphics.Bitmap.createBitmap(origW, origH, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(origBitmap)
            wallpaper.setBounds(0, 0, origW, origH)
            wallpaper.draw(canvas)

            val scale = 1f / radius.coerceIn(1, 25)
            val smallW = (origW * scale).toInt().coerceAtLeast(1)
            val smallH = (origH * scale).toInt().coerceAtLeast(1)
            val small = android.graphics.Bitmap.createScaledBitmap(origBitmap, smallW, smallH, true)
            origBitmap.recycle()

            val screenW = resources.displayMetrics.widthPixels
            val screenH = resources.displayMetrics.heightPixels
            val blurred = android.graphics.Bitmap.createScaledBitmap(small, screenW, screenH, true)
            small.recycle()
            blurred
        } catch (_: Exception) {
            null
        }
    }

    private fun applyLayoutPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val hMargin = (prefs.getInt(PREF_H_MARGIN, DEFAULT_H_MARGIN) * resources.displayMetrics.density).toInt()

        // Apply horizontal margin to content containers
        appList.setPadding(hMargin, appList.paddingTop, appList.paddingRight, appList.paddingBottom)
        widgetContainer.setPadding(hMargin, widgetContainer.paddingTop, widgetContainer.paddingRight, widgetContainer.paddingBottom)

        // Top padding is managed by updateListPaddingForWidgets()

        // Block count affects widget container max height
        val blockCount = prefs.getInt(PREF_BLOCK_COUNT, DEFAULT_BLOCK_COUNT)
        val screenHeight = resources.displayMetrics.heightPixels
        val maxWidgetHeight = screenHeight * (blockCount - 1) / blockCount
        widgetContainer.post {
            val params = widgetContainer.layoutParams
            if (widgetContainer.height > maxWidgetHeight) {
                params.height = maxWidgetHeight
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            widgetContainer.layoutParams = params
        }
    }

    // --- Notifications ---

    private fun refreshNotificationData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val mode = prefs.getString(PREF_NOTIF_MODE, NOTIF_MODE_COUNT) ?: NOTIF_MODE_COUNT
        if (mode == NOTIF_MODE_NONE) {
            notificationData = emptyMap()
            return
        }

        if (!isNotificationListenerEnabled()) {
            notificationData = emptyMap()
            return
        }

        val notifications = NotificationHolder.activeNotifications
        val grouped = mutableMapOf<String, NotifInfo>()
        for (sbn in notifications) {
            val pkg = sbn.packageName
            val existing = grouped[pkg]
            val text = sbn.notification?.extras?.getCharSequence("android.text")?.toString()
            val title = sbn.notification?.extras?.getCharSequence("android.title")?.toString()
            val summary = if (title != null && text != null) "$title: $text"
                          else text ?: title ?: ""
            grouped[pkg] = NotifInfo(
                count = (existing?.count ?: 0) + 1,
                latestText = if (summary.isNotBlank()) summary else existing?.latestText ?: ""
            )
        }
        notificationData = grouped
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = android.provider.Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ) ?: return false
        val component = ComponentName(this, NotificationService::class.java)
        return flat.contains(component.flattenToString())
    }

    // --- Widgets ---

    fun selectWidget() {
        val providers = appWidgetManager.installedProviders
        if (providers.isEmpty()) {
            Toast.makeText(this, R.string.widget_no_providers, Toast.LENGTH_SHORT).show()
            return
        }

        val labels = providers.map { prov ->
            val appLabel = try {
                val appInfo = packageManager.getApplicationInfo(prov.provider.packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) { prov.provider.packageName }
            "${prov.loadLabel(packageManager)} ($appLabel)"
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.widget_pick_title)
            .setItems(labels.toTypedArray()) { _, which ->
                bindWidget(providers[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun bindWidget(provider: AppWidgetProviderInfo) {
        val widgetId = appWidgetHost.allocateAppWidgetId()

        // If already allowed, proceed directly
        if (appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, provider.provider)) {
            onWidgetBound(widgetId, provider)
            return
        }

        // Otherwise, start the bind flow and remember pending state. Clean up on any failures.
        pendingWidgetId = widgetId
        pendingProvider = provider.provider
        @Suppress("DEPRECATION")
        val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
        }

        try {
            startActivityForResult(bindIntent, REQUEST_BIND_WIDGET)
        } catch (e: ActivityNotFoundException) {
            android.util.Log.w("ALauncher", "Widget bind activity not found: ${e.message}")
            try { appWidgetHost.deleteAppWidgetId(widgetId) } catch (_: Exception) {}
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            pendingProvider = null
            Toast.makeText(this, R.string.widget_bind_failed, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            android.util.Log.w("ALauncher", "Widget bind denied: ${e.message}")
            try { appWidgetHost.deleteAppWidgetId(widgetId) } catch (_: Exception) {}
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            pendingProvider = null
            Toast.makeText(this, R.string.widget_bind_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onWidgetBound(widgetId: Int, info: AppWidgetProviderInfo) {
        if (info.configure != null) {
            pendingWidgetId = widgetId
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = info.configure
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                },
                REQUEST_CONFIGURE_WIDGET
            )
        } else {
            finalizeWidget(widgetId)
        }
    }

    private fun finalizeWidget(widgetId: Int) {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val info = appWidgetManager.getAppWidgetInfo(widgetId)
        if (info == null) {
            appWidgetHost.deleteAppWidgetId(widgetId)
            Toast.makeText(this, R.string.widget_bind_failed, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val wrapper = createWidgetWrapper(widgetId, info)
            widgetContainer.addView(wrapper)
            activeWidgetIds.add(widgetId)
            saveWidgetOrder()
            updateListPaddingForWidgets()
        } catch (e: Exception) {
            android.util.Log.e("ALauncher", "Failed to create widget view for $widgetId: ${e.message}", e)
            try { appWidgetHost.deleteAppWidgetId(widgetId) } catch (_: Exception) {}
            Toast.makeText(this, R.string.widget_bind_failed, Toast.LENGTH_SHORT).show()
        } finally {
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            pendingProvider = null
        }
    }

    private fun createWidgetWrapper(widgetId: Int, info: AppWidgetProviderInfo): FrameLayout {
        val density = resources.displayMetrics.density
        val wrapper = WidgetFrame(this)
        wrapper.tag = widgetId
        // Create the host view; createView() already calls setAppWidget() internally.
        val hostView = appWidgetHost.createView(this, widgetId, info)
            ?: throw RuntimeException("AppWidgetHost.createView returned null for $widgetId")

        wrapper.addView(hostView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Border overlay (edit mode)
        val border = View(this).apply {
            background = GradientDrawable().apply {
                setStroke((2 * density).toInt(), Color.parseColor("#6699FF"))
                setColor(Color.TRANSPARENT)
            }
            visibility = View.GONE
            tag = "border"
        }
        wrapper.addView(border, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Remove button (edit mode)
        val removeBtn = ImageButton(this).apply {
            setImageResource(R.drawable.ic_delete)
            setBackgroundResource(R.drawable.circle_dark_bg)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding((4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt())
            visibility = View.GONE
            tag = "remove"
            setOnClickListener { removeWidget(widgetId) }
        }
        val removeLp = FrameLayout.LayoutParams((28 * density).toInt(), (28 * density).toInt()).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins((4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), 0)
        }
        wrapper.addView(removeBtn, removeLp)

        // Resize handle (edit mode)
        val resizeHandle = View(this).apply {
            setBackgroundColor(Color.parseColor("#886699FF"))
            visibility = View.GONE
            tag = "resize"
        }
        val resizeLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, (10 * density).toInt()
        ).apply { gravity = Gravity.BOTTOM }
        wrapper.addView(resizeHandle, resizeLp)

        // Touch handling for resize handle
        resizeHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    android.util.Log.d("ALauncher", "resize ACTION_DOWN widget=$widgetId y=${event.rawY}")
                    resizingWrapper = wrapper
                    resizeStartY = event.rawY
                    resizeStartHeight = wrapper.height
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (resizingWrapper == wrapper) {
                        android.util.Log.d("ALauncher", "resize ACTION_MOVE widget=$widgetId y=${event.rawY}")
                        val delta = (event.rawY - resizeStartY).toInt()
                        val minH = (MIN_WIDGET_HEIGHT_DP * density).toInt()
                        val maxH = (MAX_WIDGET_HEIGHT_DP * density).toInt()
                        val newHeight = (resizeStartHeight + delta).coerceIn(minH, maxH)
                        val lp = wrapper.layoutParams as LinearLayout.LayoutParams
                        lp.height = newHeight
                        wrapper.layoutParams = lp
                        updateListPaddingForWidgets()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    android.util.Log.d("ALauncher", "resize ACTION_UP widget=$widgetId height=${wrapper.height}")
                    if (resizingWrapper == wrapper) {
                        setWidgetHeight(widgetId, wrapper.height)
                        resizingWrapper = null
                    }
                    true
                }
                else -> false
            }
        }

        // Ensure overlay controls receive touch events in edit mode
        removeBtn.isClickable = true
        resizeHandle.isClickable = true

        // Set height
        val savedHeight = getWidgetHeight(widgetId)
        val heightPx = if (savedHeight > 0) savedHeight
                        else (info.minHeight * density).toInt().coerceAtLeast((MIN_WIDGET_HEIGHT_DP * density).toInt())
        val wrapperParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, heightPx
        ).apply { bottomMargin = (4 * density).toInt() }
        wrapper.layoutParams = wrapperParams

        // Communicate allocated size to the widget provider so it renders correctly
        val widthDp = (resources.displayMetrics.widthPixels / density).toInt()
        val heightDp = (heightPx / density).toInt()
        hostView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp)

        return wrapper
    }

    private fun enterWidgetEditMode() {
        android.util.Log.d("ALauncher", "enterWidgetEditMode called")
        isWidgetEditMode = true
        for (i in 0 until widgetContainer.childCount) {
            val child = widgetContainer.getChildAt(i)
            if (child.tag is Int) {
                child.findViewWithTag<View>("border")?.visibility = View.VISIBLE
                child.findViewWithTag<View>("remove")?.visibility = View.VISIBLE
                child.findViewWithTag<View>("resize")?.visibility = View.VISIBLE
            }
        }
        // Add "+" button at end
        val density = resources.displayMetrics.density
        val addBtn = TextView(this).apply {
            text = "+ ${getString(R.string.add_widget)}"
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#44FFFFFF"))
            setPadding(0, (12 * density).toInt(), 0, (12 * density).toInt())
            tag = "add_widget_btn"
            setOnClickListener { selectWidget() }
        }
        widgetContainer.addView(addBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = (4 * density).toInt() })
    }

    private fun exitWidgetEditMode() {
        isWidgetEditMode = false
        for (i in 0 until widgetContainer.childCount) {
            val child = widgetContainer.getChildAt(i)
            if (child.tag is Int) {
                child.findViewWithTag<View>("border")?.visibility = View.GONE
                child.findViewWithTag<View>("remove")?.visibility = View.GONE
                child.findViewWithTag<View>("resize")?.visibility = View.GONE
            }
        }
        // Remove the add button
        val addBtn = widgetContainer.findViewWithTag<View>("add_widget_btn")
        if (addBtn != null) widgetContainer.removeView(addBtn)
    }

    private fun removeWidget(widgetId: Int) {
        appWidgetHost.deleteAppWidgetId(widgetId)
        activeWidgetIds.remove(widgetId)
        // Remove wrapper from container
        for (i in 0 until widgetContainer.childCount) {
            val child = widgetContainer.getChildAt(i)
            if (child.tag == widgetId) {
                widgetContainer.removeViewAt(i)
                break
            }
        }
        clearWidgetHeight(widgetId)
        saveWidgetOrder()
        updateListPaddingForWidgets()
    }

    private fun saveWidgetOrder() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(PREF_WIDGET_ORDER, activeWidgetIds.joinToString(",")).apply()
    }

    private fun getWidgetOrder(): List<Int> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val str = prefs.getString(PREF_WIDGET_ORDER, "") ?: ""
        return if (str.isBlank()) emptyList() else str.split(",").mapNotNull { it.toIntOrNull() }
    }

    private fun getWidgetHeight(widgetId: Int): Int {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt("widget_h_$widgetId", -1)
    }

    private fun setWidgetHeight(widgetId: Int, height: Int) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt("widget_h_$widgetId", height).apply()
    }

    private fun clearWidgetHeight(widgetId: Int) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove("widget_h_$widgetId").apply()
    }

    private fun restoreWidgets() {
        widgetContainer.removeAllViews()
        activeWidgetIds.clear()
        // Migrate from old StringSet format if needed
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val orderStr = prefs.getString(PREF_WIDGET_ORDER, null)
        val ids: List<Int> = if (orderStr != null && orderStr.isNotBlank()) {
            orderStr.split(",").mapNotNull { it.toIntOrNull() }
        } else {
            // Try migrating from old format
            val oldSet = prefs.getStringSet(PREF_WIDGET_IDS_OLD, null)
            if (oldSet != null) {
                val migrated = oldSet.mapNotNull { it.toIntOrNull() }
                prefs.edit().putString(PREF_WIDGET_ORDER, migrated.joinToString(","))
                    .remove(PREF_WIDGET_IDS_OLD).apply()
                migrated
            } else emptyList()
        }

        for (widgetId in ids) {
            val info = appWidgetManager.getAppWidgetInfo(widgetId)
            if (info != null) {
                val wrapper = createWidgetWrapper(widgetId, info)
                widgetContainer.addView(wrapper)
                activeWidgetIds.add(widgetId)
            } else {
                appWidgetHost.deleteAppWidgetId(widgetId)
            }
        }
        // Clean up stale IDs
        if (activeWidgetIds.size != ids.size) saveWidgetOrder()
        updateListPaddingForWidgets()
    }

    private fun refreshWidgetSizes() {
        for (i in 0 until widgetContainer.childCount) {
            val child = widgetContainer.getChildAt(i)
            val wId = child.tag as? Int ?: continue
            val savedHeight = getWidgetHeight(wId)
            if (savedHeight > 0) {
                val lp = child.layoutParams as LinearLayout.LayoutParams
                lp.height = savedHeight
                child.layoutParams = lp
            }
        }
        updateListPaddingForWidgets()
    }

    private fun updateListPaddingForWidgets() {
        // Double-post to ensure we read the height after any pending layout changes
        widgetContainer.post {
            widgetContainer.post {
                val density = resources.displayMetrics.density
                val vMargin = (getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getInt(PREF_V_MARGIN, DEFAULT_V_MARGIN) * density).toInt()
                val topPadding = if (widgetContainer.visibility == View.VISIBLE && activeWidgetIds.isNotEmpty()) {
                    widgetContainer.height + vMargin
                } else {
                    (60 * density).toInt() + vMargin
                }
                appList.setPadding(appList.paddingLeft, topPadding, appList.paddingRight, appList.paddingBottom)
                updateSidebarPosition()
            }
        }
    }

    private fun updateSidebarPosition() {
        sidebar.setPadding(sidebar.paddingLeft, appList.paddingTop, sidebar.paddingRight, appList.paddingBottom)
    }

    // --- App loading ---

    private fun loadLaunchableApps(): List<AppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = packageManager.queryIntentActivities(launcherIntent, 0)
        return resolved
            .mapNotNull { info ->
                val activityInfo = info.activityInfo ?: return@mapNotNull null
                val label = info.loadLabel(packageManager)?.toString().orEmpty()
                val packageName = activityInfo.packageName
                val activityName = activityInfo.name
                val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val icon = try { info.loadIcon(packageManager) } catch (_: Exception) { null }
                AppInfo(
                    label = if (label.isBlank()) packageName else label,
                    packageName = packageName,
                    launchIntent = launchIntent,
                    icon = icon
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private fun openSystemSearch() {
        val webSearchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, "")
        }
        val assistIntent = Intent(Intent.ACTION_ASSIST)
        when {
            webSearchIntent.resolveActivity(packageManager) != null -> startActivity(webSearchIntent)
            assistIntent.resolveActivity(packageManager) != null -> startActivity(assistIntent)
            else -> Toast.makeText(this, R.string.search_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resolveTypeface(fontFamily: String): Typeface {
        val app = application as? LauncherApp
        val custom = app?.customTypeface
        if (custom != null && fontFamily == CUSTOM_FONT_KEY) return custom
        return Typeface.create(fontFamily, Typeface.NORMAL)
    }

    private inner class WidgetFrame(ctx: Context) : FrameLayout(ctx) {
        private var downX = 0f
        private var downY = 0f
        private val longPressRunnable = Runnable {
            if (!isWidgetEditMode) enterWidgetEditMode()
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            val slop = android.view.ViewConfiguration.get(context).scaledTouchSlop
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = ev.x
                    downY = ev.y
                    postDelayed(longPressRunnable, android.view.ViewConfiguration.getLongPressTimeout().toLong())
                }
                MotionEvent.ACTION_MOVE -> {
                    if (kotlin.math.abs(ev.x - downX) > slop || kotlin.math.abs(ev.y - downY) > slop) {
                        removeCallbacks(longPressRunnable)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    removeCallbacks(longPressRunnable)
                }
            }
            return false
        }
    }

    companion object {
        const val PREFS_NAME = "launcher_prefs"
        const val PREF_FONT = "font_family"
        const val PREF_SPACING = "item_spacing"
        const val PREF_DARKNESS = "wallpaper_darkness"
        const val PREF_FONT_SIZE = "font_size"
        const val PREF_THEME = "theme"
        const val PREF_NOTIF_MODE = "notification_mode"
        const val PREF_WIDGET_ORDER = "widget_order"
        const val PREF_WIDGET_IDS_OLD = "widget_ids"
        const val PREF_WIDGETS_DIRTY = "widgets_dirty"
        const val PREF_ANIM_STYLE = "anim_style"
        const val PREF_WAVE_SHIFT = "wave_shift"
        const val PREF_WAVE_SCALE = "wave_scale"
        const val PREF_ALIGNMENT = "alignment"
        const val PREF_H_MARGIN = "h_margin"
        const val PREF_V_MARGIN = "v_margin"
        const val PREF_BLOCK_COUNT = "block_count"
        const val PREF_FAVORITES = "favorites"
        const val DEFAULT_FONT = "sans-serif-light"
        const val DEFAULT_SPACING = 12
        const val DEFAULT_DARKNESS = 40
        const val DEFAULT_FONT_SIZE = 22
        const val DEFAULT_WAVE_SHIFT = 25
        const val DEFAULT_WAVE_SCALE = 8 // represents 1.8x
        const val PREF_HIGHLIGHT_INTENSITY = "highlight_intensity"
        const val PREF_FADE_RADIUS = "fade_radius"
        const val DEFAULT_HIGHLIGHT_INTENSITY = 5
        const val DEFAULT_FADE_RADIUS = 10
        const val WALLPAPER_EFFECT_DARKEN = "darken"
        const val WALLPAPER_EFFECT_BLUR = "blur"
        const val WALLPAPER_EFFECT_COLOR = "color"
        const val PREF_WALLPAPER_EFFECT = "wallpaper_effect"
        const val PREF_BLUR_RADIUS = "blur_radius"
        const val PREF_COLOR_TINT = "color_tint"
        const val DEFAULT_BLUR_RADIUS = 15
        const val DEFAULT_COLOR_TINT = "#1A237E"
        const val PREF_NOTIF_SWIPE = "notif_swipe_enabled"
        const val PREF_ICON_SIZE = "icon_size"
        const val DEFAULT_ICON_SIZE = 36
        const val PREF_ICON_PACK = "icon_pack"
        const val PREF_NERD_FONT = "nerd_font_enabled"
        const val PREF_ICON_MODE = "icon_mode"
        const val ICON_MODE_REGULAR = "regular"
        const val ICON_MODE_NERD = "nerd"
        const val ICON_MODE_NONE = "none"
        const val DEFAULT_H_MARGIN = 24
        const val DEFAULT_V_MARGIN = 0
        const val DEFAULT_BLOCK_COUNT = 2
        const val SIDEBAR_FAVORITES = "★"
        const val SIDEBAR_OTHER = "#"
        const val CUSTOM_FONT_KEY = "_custom_ttf"
        const val NOTIF_MODE_COUNT = "count"
        const val NOTIF_MODE_TEXT = "text"
        const val NOTIF_MODE_NONE = "none"
        const val APPWIDGET_HOST_ID = 1024
        const val REQUEST_BIND_WIDGET = 9003
        const val REQUEST_CONFIGURE_WIDGET = 9002
        const val MIN_WIDGET_HEIGHT_DP = 60
        const val MAX_WIDGET_HEIGHT_DP = 400
        val DEFAULT_FAVORITES = setOf(
            "com.android.settings",
            "com.android.chrome",
            "com.google.android.gm",
            "com.google.android.youtube",
            "com.google.android.apps.maps"
        )
    }
}

// Shared notification holder for service → activity communication
object NotificationHolder {
    @Volatile
    var activeNotifications: Array<StatusBarNotification> = emptyArray()
    @Volatile
    var service: NotificationService? = null
}

data class NotifInfo(val count: Int, val latestText: String)

data class AppInfo(
    val label: String,
    val packageName: String,
    val launchIntent: Intent,
    val icon: Drawable? = null
)

private class NerdFontSpan(private val typeface: Typeface) : android.text.style.MetricAffectingSpan() {
    override fun updateDrawState(tp: android.text.TextPaint) {
        tp.typeface = typeface
    }
    override fun updateMeasureState(tp: android.text.TextPaint) {
        tp.typeface = typeface
    }
}

private class AppListAdapter(
    private val inflater: LayoutInflater,
    private val apps: List<AppInfo>,
    private val typeface: Typeface,
    private val spacingDp: Int,
    private val fontSizeSp: Int,
    private val notifData: Map<String, NotifInfo>,
    private val notifMode: String,
    private val centerAlign: Boolean,
    private val iconSizeDp: Int = 36,
    private val iconPackResolver: IconPackResolver? = null,
    private val nerdTypeface: Typeface? = null,
    private val iconMode: String = MainActivity.ICON_MODE_REGULAR,
    private val showHeaders: Boolean = false
) : BaseAdapter() {

    // Flat list: String (section header) or AppInfo
    private val displayItems: List<Any> = buildDisplayItems()

    private fun buildDisplayItems(): List<Any> {
        if (!showHeaders || apps.isEmpty()) return apps
        val result = mutableListOf<Any>()
        var lastHeader = ""
        for (app in apps) {
            val header = app.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
            if (header != lastHeader) {
                result.add(header)
                lastHeader = header
            }
            result.add(app)
        }
        return result
    }

    fun getAppInfo(position: Int): AppInfo? = displayItems.getOrNull(position) as? AppInfo

    private val nerdGlyphs = mapOf(
        "com.android.chrome" to "\uF268",
        "com.google.android.gm" to "\uF0E0",
        "com.google.android.youtube" to "\uF167",
        "com.google.android.apps.maps" to "\uF279",
        "com.android.settings" to "\uF013",
        "com.google.android.dialer" to "\uF095",
        "com.android.dialer" to "\uF095",
        "com.google.android.apps.messaging" to "\uF075",
        "com.android.mms" to "\uF075",
        "com.google.android.calendar" to "\uF073",
        "com.google.android.deskclock" to "\uF017",
        "com.android.camera" to "\uF030",
        "com.google.android.camera" to "\uF030",
        "com.google.android.apps.photos" to "\uF03E",
        "com.android.vending" to "\uF3A5",
        "com.google.android.music" to "\uF001",
        "com.spotify.music" to "\uF1BC",
        "com.whatsapp" to "\uF232",
        "com.twitter.android" to "\uF099",
        "com.instagram.android" to "\uF16D",
        "com.facebook.katana" to "\uF09A",
        "com.slack" to "\uF198",
        "org.telegram.messenger" to "\uF2C6",
        "com.google.android.apps.docs" to "\uF15C",
        "com.google.android.gms" to "\uF1A0"
    )

    override fun getCount(): Int = displayItems.size
    override fun getItem(position: Int): Any = displayItems[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getViewTypeCount(): Int = if (showHeaders) 2 else 1
    override fun getItemViewType(position: Int): Int {
        if (!showHeaders) return 0
        return if (displayItems[position] is String) 0 else 1
    }
    override fun areAllItemsEnabled(): Boolean = !showHeaders
    override fun isEnabled(position: Int): Boolean = displayItems[position] is AppInfo

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = displayItems[position]
        if (item is String) return getHeaderView(item, convertView, parent)
        val app = item as AppInfo
        val view = convertView ?: inflater.inflate(R.layout.item_app, parent, false)

        val iconView = view.findViewById<ImageView>(R.id.appIcon)
        val nameView = view.findViewById<TextView>(R.id.appName)
        val notifView = view.findViewById<TextView>(R.id.notificationText)
        val badgeView = view.findViewById<TextView>(R.id.notifBadge)

        // Icon pack or default icon
        val showRegularIcon = iconMode == MainActivity.ICON_MODE_REGULAR
        val packIcon = if (showRegularIcon) iconPackResolver?.resolve(app.packageName) else null
        val displayIcon = if (showRegularIcon) (packIcon ?: app.icon) else null
        if (displayIcon != null) {
            iconView.setImageDrawable(displayIcon)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.setImageDrawable(null)
            iconView.visibility = View.GONE
        }

        // Dynamic icon sizing
        val density = view.resources.displayMetrics.density
        val sizePx = (iconSizeDp * density).toInt()
        val iconFrame = iconView.parent as? FrameLayout
        if (showRegularIcon) {
            iconFrame?.layoutParams?.width = sizePx
            iconFrame?.layoutParams?.height = sizePx
            iconView.layoutParams?.width = sizePx
            iconView.layoutParams?.height = sizePx
            iconFrame?.visibility = View.VISIBLE
        } else {
            iconFrame?.layoutParams?.width = 0
            iconFrame?.layoutParams?.height = 0
            iconFrame?.visibility = View.GONE
        }

        // Nerd font prefix (only in nerd mode)
        val glyph = if (iconMode == MainActivity.ICON_MODE_NERD && nerdTypeface != null) nerdGlyphs[app.packageName] else null
        if (glyph != null) {
            val spannable = android.text.SpannableString("$glyph ${app.label}")
            spannable.setSpan(
                NerdFontSpan(nerdTypeface!!),
                0, glyph.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Scale nerd glyph to match icon size parameter
            val glyphScale = iconSizeDp.toFloat() / fontSizeSp.coerceAtLeast(1)
            spannable.setSpan(
                android.text.style.RelativeSizeSpan(glyphScale.coerceIn(0.5f, 3f)),
                0, glyph.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            nameView.text = spannable
        } else {
            nameView.text = app.label
        }
        nameView.typeface = typeface
        nameView.textSize = fontSizeSp.toFloat()

        val paddingPx = (spacingDp * view.resources.displayMetrics.density).toInt()
        val root = view as ViewGroup
        root.setPadding(root.paddingLeft, paddingPx, root.paddingRight, paddingPx)

        // Center alignment for entire row (icon + label)
        val nameContainer = nameView.parent as? LinearLayout
        if (centerAlign) {
            nameView.gravity = Gravity.CENTER_HORIZONTAL
            nameContainer?.let {
                (it.layoutParams as? LinearLayout.LayoutParams)?.apply { width = LinearLayout.LayoutParams.WRAP_CONTENT; weight = 0f }
            }
            nameView.layoutParams?.width = LinearLayout.LayoutParams.WRAP_CONTENT
            if (root is LinearLayout) root.gravity = Gravity.CENTER
        } else {
            nameView.gravity = Gravity.START
            nameContainer?.let {
                (it.layoutParams as? LinearLayout.LayoutParams)?.apply { width = 0; weight = 1f }
            }
            nameView.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT
            if (root is LinearLayout) root.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        notifView.gravity = if (centerAlign) Gravity.CENTER_HORIZONTAL else Gravity.START

        val notif = notifData[app.packageName]

        // Badge count on icon
        if (notif != null && notifMode == MainActivity.NOTIF_MODE_COUNT) {
            badgeView.text = if (notif.count > 99) "99+" else notif.count.toString()
            badgeView.visibility = View.VISIBLE
            notifView.visibility = View.GONE
        }
        // Text mode: inline text below app name
        else if (notif != null && notifMode == MainActivity.NOTIF_MODE_TEXT) {
            badgeView.visibility = View.GONE
            if (notif.latestText.isNotBlank()) {
                notifView.text = notif.latestText
                notifView.visibility = View.VISIBLE
            } else {
                notifView.visibility = View.GONE
            }
        }
        // Off or no notification
        else {
            badgeView.visibility = View.GONE
            notifView.visibility = View.GONE
        }

        return view
    }

    private fun getHeaderView(letter: String, convertView: View?, parent: ViewGroup): View {
        val view = (convertView as? TextView) ?: TextView(parent.context).apply {
            setTextColor(Color.parseColor("#CCFFFFFF"))
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, (16 * resources.displayMetrics.density).toInt(), 0, (4 * resources.displayMetrics.density).toInt())
        }
        if (view.text != letter) {
            view.text = letter
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(200).start()
        }
        view.typeface = android.graphics.Typeface.create(typeface, android.graphics.Typeface.BOLD)
        view.gravity = if (centerAlign) Gravity.CENTER_HORIZONTAL else Gravity.START
        return view
    }
}

