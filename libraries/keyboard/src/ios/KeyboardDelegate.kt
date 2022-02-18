package featurea.keyboard

import featurea.runtime.Module
import featurea.runtime.Component

actual class KeyboardDelegate actual constructor(override val module: Module) : Component {
    actual fun show(keyboardType: KeyboardType): Unit = TODO()
    actual fun hide(): Unit = TODO()
}
