package com.alauncher

import android.app.SearchManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val favoritePackageNames = setOf(
        "com.android.settings",
        "com.android.chrome",
        "com.google.android.gm",
        "com.google.android.youtube",
        "com.google.android.apps.maps"
    )

    private val rows = mutableListOf<ListRow>()

    private lateinit var appList: ListView
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appList = findViewById(R.id.appList)
        searchButton = findViewById(R.id.searchButton)

        searchButton.setOnClickListener { openSystemSearch() }

        appList.setOnItemClickListener { _, _, position, _ ->
            val row = rows[position]
            if (row is ListRow.App) {
                try {
                    startActivity(row.launchIntent)
                } catch (_: Exception) {
                    Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRows()
    }

    private fun loadRows() {
        rows.clear()

        val apps = loadLaunchableApps()
        val favorites = apps.filter { favoritePackageNames.contains(it.packageName) }
        val others = apps.filterNot { favoritePackageNames.contains(it.packageName) }

        if (favorites.isNotEmpty()) {
            rows.add(ListRow.Header(getString(R.string.favorites_section_title)))
            favorites.forEach { rows.add(ListRow.App(it.label, it.packageName, it.launchIntent)) }
        }

        rows.add(ListRow.Header(getString(R.string.apps_section_title)))
        others.forEach { rows.add(ListRow.App(it.label, it.packageName, it.launchIntent)) }

        appList.adapter = LauncherListAdapter(layoutInflater, rows)
    }

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
                AppInfo(
                    label = if (label.isBlank()) packageName else label,
                    packageName = packageName,
                    launchIntent = launchIntent
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
}

private data class AppInfo(
    val label: String,
    val packageName: String,
    val launchIntent: Intent
)

private sealed class ListRow {
    data class Header(val title: String) : ListRow()
    data class App(
        val label: String,
        val packageName: String,
        val launchIntent: Intent
    ) : ListRow()
}

private class LauncherListAdapter(
    private val inflater: LayoutInflater,
    private val rows: List<ListRow>
) : BaseAdapter() {

    override fun getCount(): Int = rows.size

    override fun getItem(position: Int): ListRow = rows[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean = getItem(position) is ListRow.App

    override fun getViewTypeCount(): Int = 2

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListRow.Header -> 0
            is ListRow.App -> 1
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        return when (item) {
            is ListRow.Header -> bindHeader(item, convertView, parent)
            is ListRow.App -> bindApp(item, convertView, parent)
        }
    }

    private fun bindHeader(item: ListRow.Header, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        val text = view.findViewById<TextView>(android.R.id.text1)
        text.text = item.title
        text.setTypeface(text.typeface, Typeface.BOLD)
        text.isAllCaps = true
        return view
    }

    private fun bindApp(item: ListRow.App, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)
        text1.text = item.label
        text2.text = item.packageName
        return view
    }
}

