package featurea.dialog

import featurea.runtime.Component
import featurea.runtime.Module

actual class AlertService actual constructor(override val module: Module) : Component {
    actual fun alert(title: String?, text: String, vararg buttons: String, complete: (String) -> Unit): Unit = TODO()
    actual fun toast(text: String): Unit = TODO()
}
