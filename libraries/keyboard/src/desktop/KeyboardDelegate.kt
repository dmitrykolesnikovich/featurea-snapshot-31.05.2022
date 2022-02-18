package featurea.keyboard

import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import

actual class KeyboardDelegate actual constructor(override val module: Module) : Component {

    private val keyboard: Keyboard = import()

    actual fun show(keyboardType: KeyboardType) {
        keyboard.fireShowKeyboard(0, 0)
    }

    actual fun hide() {
        keyboard.fireHideKeyboard()
    }

}
