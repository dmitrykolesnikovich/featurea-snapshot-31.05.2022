@file:JvmName("Package")

package featurea.window

import featurea.runtime.DependencyBuilder

/*dependencies*/

actual fun DependencyBuilder.includeExternals() {
    include(featurea.desktop.artifact)

    WindowPlugin {
        "initWindow" to initWindow
        "MainPanel" to ::MainPanel
    }
}
