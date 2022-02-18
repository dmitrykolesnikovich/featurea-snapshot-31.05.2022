package featurea.orientation

import featurea.runtime.DependencyBuilder
import featurea.window.WindowPlugin

actual fun DependencyBuilder.includeExternals() {
    WindowPlugin {
        "OrientationChangeListener" to ::OrientationChangeListener
    }
}
