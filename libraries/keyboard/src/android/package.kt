@file:JvmName("Package")

package featurea.keyboard

import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    KeyboardPlugin {
        "KeyboardEventProducer" to ::KeyboardEventProducer
    }
}
