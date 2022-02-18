package featurea.android.simulator

import android.content.Context
import android.util.AttributeSet
import featurea.android.FeatureaDialogPreference
import featurea.runtime.import
import featurea.settings.SettingsService

class WifiSettingsPreference(context: Context, attrs: AttributeSet) : FeatureaDialogPreference(context, attrs) {

    private val settingsService: SettingsService by lazy { import() }

    val wifiSsid: String get() = settingsService["wifiSsid", ""]
    val wifiPassword: String get() = settingsService["wifiPassword", ""]

    init {
        dialogLayoutResource = R.layout.wifi_settings_layout
    }

    fun setSsidPassword(ssid: String, password: String) {
        settingsService["wifiSsid"] = ssid
        settingsService["wifiPassword"] = password
        summary = ssid
    }

}
