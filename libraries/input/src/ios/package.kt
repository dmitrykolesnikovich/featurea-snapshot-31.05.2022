package featurea.input

import featurea.runtime.DependencyBuilder

actual fun DependencyBuilder.includeExternals() {
    "MainViewProvider" to ::MainViewProvider
    "TouchEventProducer" to ::TouchEventProducer
}
