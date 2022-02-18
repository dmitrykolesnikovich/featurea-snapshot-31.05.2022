package featurea.browser

import featurea.runtime.Module
import featurea.runtime.Component
import kotlinx.browser.window as jsWindow

actual class Browser actual constructor(override val module: Module) : Component {

    actual fun open(url: String) {
        jsWindow.open(url, "_blank")?.focus()
    }

}
