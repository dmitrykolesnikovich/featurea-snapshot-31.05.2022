package featurea

import featurea.runtime.Module
import featurea.runtime.Component
import platform.UIKit.UIDevice

actual class Device actual constructor(override val module: Module) : Component {
    actual val id: String get() = UIDevice.currentDevice.identifierForVendor!!.UUIDString
}