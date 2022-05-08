@file:JvmName("Package")

package featurea.input

import featurea.app.ApplicationPlugin
import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    "TouchEventProducer" to ::TouchEventProducer

    // just for try todo revert to `InputPlugin`
    ApplicationPlugin {
        "MainView" to ::MainView
    }
}
