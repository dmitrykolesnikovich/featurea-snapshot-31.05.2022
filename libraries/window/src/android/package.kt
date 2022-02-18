@file:JvmName("Package")

package featurea.window

import featurea.ApplicationPlugin
import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    include(featurea.android.artifact)

    "MainActivityContentView" to ::MainActivityContentView

    ApplicationPlugin {
        "MainRender" to ::MainRender
    }
}
