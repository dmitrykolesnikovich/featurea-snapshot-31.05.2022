@file:JvmName("WindowDelegate")

package featurea.window

import featurea.layout.View
import featurea.runtime.Component
import featurea.runtime.Module
import javafx.scene.control.Control

actual class WindowElement(val control: Control)

actual class WindowDelegate actual constructor(override val module: Module) : Component {
    actual fun appendView(view: View): Unit = TODO()
    actual fun removeView(view: View): Unit = TODO()
}
