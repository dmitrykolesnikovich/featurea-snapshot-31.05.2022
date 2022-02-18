package featurea.dialog

import featurea.runtime.Component
import featurea.runtime.Module

expect class AlertService(module: Module) : Component {
    /** @param buttons two valid values: 1) negative and positive, 2) negative only */
    fun alert(title: String?, text: String, vararg buttons: String, complete: (button: String) -> Unit)
    fun toast(text: String)
}
