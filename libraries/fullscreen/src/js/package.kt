package featurea.fullscreen

import featurea.runtime.DependencyBuilder
import featurea.window.WindowPlugin

actual fun DependencyBuilder.includeExternals() {
    WindowPlugin {
        "FullScreenButtonProvider" to ::FullScreenButtonProvider
    }
}