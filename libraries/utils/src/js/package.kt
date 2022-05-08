package featurea.utils

import featurea.System
import featurea.runtime.DependencyBuilder
import kotlinx.browser.window

actual fun DependencyBuilder.includeExternals() {
    static {
        val system: System = import()
        system.contentRoots.add(window.location.href.parent)
    }
}
