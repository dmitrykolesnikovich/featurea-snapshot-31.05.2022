package featurea

import featurea.runtime.Component
import featurea.runtime.Module

actual class Device actual constructor(override val module: Module) : Component {
    actual val id: String get() = System.target.name
}