package featurea.window

import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    include(featurea.ios.artifact)

    WindowPlugin {
        "MainController" to ::MainController
    }
}
