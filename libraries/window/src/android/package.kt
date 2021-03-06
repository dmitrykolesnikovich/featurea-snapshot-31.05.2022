@file:JvmName("Package")

package featurea.window

import featurea.app.ApplicationPlugin
import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    include(featurea.android.artifact)
    ApplicationPlugin {
        "MainActivityContentView" to ::MainActivityContentView
        "MainRender" to ::MainRender
    }
}
