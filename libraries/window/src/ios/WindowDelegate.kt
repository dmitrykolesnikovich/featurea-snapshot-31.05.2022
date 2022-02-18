package featurea.window

import featurea.layout.View
import featurea.runtime.Module
import featurea.runtime.Component
import platform.UIKit.UIView

actual class WindowElement(val view: UIView)

actual class WindowDelegate actual constructor(override val module: Module) : Component {
    actual fun appendView(view: View): Unit = TODO()
    actual fun removeView(view: View): Unit = TODO()
}
