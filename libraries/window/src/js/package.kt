package featurea.window

import featurea.js.ProgressView
import featurea.runtime.DependencyBuilder

/*dependencies*/

actual fun DependencyBuilder.includeExternals() {
    include(featurea.js.artifact)

    WindowPlugin {
        "MainView" to ::MainView
        "ProgressView" to ::ProgressView
    }
}
