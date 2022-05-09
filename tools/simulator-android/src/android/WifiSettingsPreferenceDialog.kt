package featurea.android.simulator

import android.os.Bundle
import android.view.View
import android.widget.EditText
import featurea.android.FeatureaPreferenceDialogFragmentCompat

class WifiSettingsPreferenceDialog : FeatureaPreferenceDialogFragmentCompat<WifiSettingsPreference>() {

    private lateinit var ssidEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onDialogClosed(isPositiveResult: Boolean) {
        if (!isPositiveResult) return
        val ssid = ssidEditText.text.toString()
        val password = passwordEditText.text.toString()
        preference.setSsidPassword(ssid, password)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        ssidEditText = view.findViewById(R.id.ssidEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        ssidEditText.setText(preference.wifiSsid)
        passwordEditText.setText(preference.wifiPassword)
        ssidEditText.setSelection(ssidEditText.text.length)
    }

}

// constructor
fun newWifiSettingsPreferenceDialog(key: String): WifiSettingsPreferenceDialog = WifiSettingsPreferenceDialog().apply {
    arguments = Bundle(1).apply { putString("key", key) }
}
