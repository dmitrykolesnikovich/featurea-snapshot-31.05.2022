package featurea.android.simulator

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import featurea.android.FeatureaActivity
import featurea.android.FeatureaPreferencesFragment
import featurea.android.MainActivityProxy
import featurea.android.navigationBar.hideNavigationBar
import featurea.android.navigationBar.showNavigationBar
import featurea.android.sharedPreferences
import featurea.layout.AllOrientations
import featurea.layout.LandscapeOrientations
import featurea.layout.PortraitOrientations
import featurea.orientation.OrientationService
import featurea.runtime.Module
import featurea.runtime.import
import featurea.settings.SettingsService

class SettingsFragment : FeatureaPreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val mainActivity: FeatureaActivity by lazy { import(MainActivityProxy) }
    private val settingsService: SettingsService by lazy { import() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        mainActivity.supportActionBar?.setTitle(R.string.settings)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is WifiSettingsPreference) {
            val fragment = newWifiSettingsPreferenceDialog(preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. wifi settings
        val wifiSettingsPreference = findPreference<Preference>("wifi_settings_preference")!!
        wifiSettingsPreference.summary = settingsService["wifiSsid", ""]

        // 2. screen saver theme
        findPreference<Preference>("screen_saver_preference")!!.apply {
            summary = settingsService["screen_saver_preference", ""]
            setOnPreferenceChangeListener { preference, newValue ->
                summary = newValue as String
                true
            }
        }

        // action bar
        findPreference<Preference>("action_bar_preference")!!.apply {
            summary = if (sharedPreferences.getBoolean("action_bar_preference", false)) "Visible" else "Invisible"
            setOnPreferenceChangeListener { preference, newValue ->
                summary = if (newValue as Boolean) "Visible" else "Invisible"
                true
            }
        }

        // status bar
        findPreference<Preference>("status_bar_preference")!!.apply {
            summary = if (sharedPreferences.getBoolean("status_bar_preference", true)) "Visible" else "Invisible"
            setOnPreferenceChangeListener { preference, newValue ->
                summary = if (newValue as Boolean) "Visible" else "Invisible"
                true
            }
        }

        // orientation
        findPreference<Preference>("orientation_preference")!!.apply {
            summary = settingsService["orientation_preference", ""]
            setOnPreferenceChangeListener { preference, newValue ->
                summary = newValue as String
                true
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        mainActivity.updateStatusBarPreference()
    }

}

fun FeatureaActivity.updateStatusBarPreference() {
    if (sharedPreferences.getBoolean("status_bar_preference", true)) {
        window.showNavigationBar()
    } else {
        window.hideNavigationBar()
    }
}

fun FeatureaActivity.updateActionBarPreference() {
    if (sharedPreferences.getBoolean("action_bar_preference", false)) {
        supportActionBar?.show()
    } else {
        supportActionBar?.hide()
    }
}

fun Module.updateOrientationPreference() {
    val orientationService: OrientationService = importComponent()
    val settingsService: SettingsService = importComponent()
    val orientation = settingsService["orientation_preference", "Dynamic"]
    when (orientation) {
        "Vertical" -> orientationService.allowedOrientations = PortraitOrientations
        "Horizontal" -> orientationService.allowedOrientations = LandscapeOrientations
        "Dynamic" -> orientationService.allowedOrientations = AllOrientations
    }
}
