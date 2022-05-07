package featurea

import featurea.runtime.DependencyBuilder
import featurea.utils.parent
import kotlinx.browser.window
import featurea.js.ApplicationProgressView

actual fun DependencyBuilder.includeExternals() {
    "ApplicationProgressView" to ::ApplicationProgressView

    static {
        val system: System = import()
        system.contentRoots.add(window.location.href.parent)
    }
}
