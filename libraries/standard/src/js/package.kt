package featurea

import featurea.runtime.DependencyBuilder
import featurea.utils.parent
import kotlinx.browser.window

actual fun DependencyBuilder.includeExternals() {
    static {
        val system: System = import()

        system.contentRoots.add(window.location.href.parent)
    }
}
