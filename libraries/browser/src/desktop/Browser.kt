package featurea.browser

import featurea.runtime.Module
import featurea.runtime.Component

actual class Browser actual constructor(override val module: Module): Component {
    actual fun open(url: String) {
    }

}