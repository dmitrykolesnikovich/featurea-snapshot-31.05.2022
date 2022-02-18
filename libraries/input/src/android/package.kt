@file:JvmName("Package")

package featurea.input

import featurea.ApplicationPlugin
import featurea.runtime.DependencyBuilder
import featurea.window.WindowPlugin

actual fun DependencyBuilder.includeExternals() {
    "TouchEventProducer" to ::TouchEventProducer

    // just for try todo revert to `InputPlugin`
    ApplicationPlugin {
        "MainView" to ::MainView
    }
}
