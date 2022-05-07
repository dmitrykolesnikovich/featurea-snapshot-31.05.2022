package featurea.utils

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.System

actual class Device actual constructor(override val module: Module) : Component {
    actual val id: String get() = System.target.name
}