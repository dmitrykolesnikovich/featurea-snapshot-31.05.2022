package featurea.keyboard

import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.view.inputmethod.InputMethod.SHOW_FORCED
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.android.contentView
import featurea.android.getInputMethodManager
import featurea.android.navigationBar.hideNavigationBar
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.settings.SettingsService

actual class KeyboardDelegate actual constructor(override val module: Module) : Component {

    private val mainActivity: FeatureaActivity = import(MainActivityProxy)
    private val numberInputTextField: EditText by lazy { import<KeyEventProducer>().numberInputTextField }
    private val settingsService: SettingsService = import()

    actual fun show(keyboardType: KeyboardType) {
        mainActivity.runOnUiThread {
            val inputMethodManager: InputMethodManager = mainActivity.getInputMethodManager()
            val inputType: Int = keyboardType.toAndroidInputType()
            if (inputType == TYPE_NUMBER_FLAG_DECIMAL) {
                numberInputTextField.requestFocus()
                inputMethodManager.showSoftInput(numberInputTextField, SHOW_FORCED)
            }
        }
    }

    actual fun hide() {
        val inputMethodManager: InputMethodManager = mainActivity.getInputMethodManager()
        inputMethodManager.hideSoftInputFromWindow(mainActivity.contentView.windowToken, 0)
        if (!settingsService["isNavigationBarVisible", false]) {
            mainActivity.runOnUiThread { mainActivity.window.hideNavigationBar() }
        }
    }

}
