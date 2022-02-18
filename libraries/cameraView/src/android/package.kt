@file:JvmName("Package")

package featurea.cameraView

import featurea.runtime.DependencyBuilder
import featurea.window.WindowPlugin

actual fun DependencyBuilder.includeExternals() {
    WindowPlugin {
        "CameraElementProvider" to ::CameraElementProvider
    }
}
