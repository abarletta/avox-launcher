package com.alauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SettingsMenuFragment : Fragment() {
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
    }
}
