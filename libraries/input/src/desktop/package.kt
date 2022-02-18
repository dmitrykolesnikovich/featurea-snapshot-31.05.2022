@file:JvmName("Package")

package featurea.input

import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    InputPlugin {
        "MouseEventProducer" to ::MouseEventProducer
    }
}
