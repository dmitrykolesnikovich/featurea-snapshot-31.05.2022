package featurea

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.utils.target

actual class Device actual constructor(override val module: Module) : Component {
    actual val id: String get() = System.target.name
}