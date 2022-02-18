package featurea

import featurea.runtime.Component
import featurea.runtime.Module

expect class Device(module: Module) : Component {
    val id: String
}