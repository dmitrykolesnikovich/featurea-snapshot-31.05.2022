package featurea.dialog

import android.app.AlertDialog
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

actual class AlertService actual constructor(override val module: Module) : Component {

    private val mainActivity: FeatureaActivity = import(MainActivityProxy)

    actual fun alert(title: String?, text: String, vararg buttons: String, complete: (button: String) -> Unit) {
        mainActivity.runOnUiThread {
            AlertDialog.Builder(mainActivity)
                .setTitle(title)
                .setMessage(text)
                .apply {
                    when (buttons.size) {
                        0 -> {
                            setNegativeButton("OK", null)
                        }
                        1 -> {
                            setNegativeButton(buttons[0]) { _, _ -> complete(buttons[0]) }
                        }
                        2 -> {
                            setNegativeButton(buttons[0]) { _, _ -> complete(buttons[0]) }
                            setPositiveButton(buttons[1]) { _, _ -> complete(buttons[1]) }
                        }
                    }
                }
                .create()
                .show()
        }
    }

    actual fun toast(text: String) {
        mainActivity.runOnUiThread {
            Toast.makeText(mainActivity, text, LENGTH_LONG).show()
        }
    }

}
