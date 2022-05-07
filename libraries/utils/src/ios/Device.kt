package featurea.utils

import featurea.runtime.Component
import featurea.runtime.Module
import platform.UIKit.UIDevice

actual class Device actual constructor(override val module: Module) : Component {
    actual val id: String get() = UIDevice.currentDevice.identifierForVendor!!.UUIDString
}