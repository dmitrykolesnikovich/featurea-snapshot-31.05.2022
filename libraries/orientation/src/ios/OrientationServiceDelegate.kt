@file:Suppress("EXPERIMENTAL_API_USAGE")

package featurea.orientation

import featurea.utils.log
import featurea.ios.UIApplicationDelegate
import featurea.ios.UIApplicationProxy
import featurea.layout.*
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import platform.Foundation.setValue
import platform.UIKit.*

internal actual class OrientationServiceDelegate actual constructor(override val module: Module) : Component {

    private val appDelegate: UIApplicationDelegate = import(UIApplicationProxy)

    @ExperimentalUnsignedTypes
    actual var allowedOrientations: Collection<Orientation> = AllOrientations
        set(value) {
            field = value
            log("OrientationServiceDelegate.allowedOrientations: ${value.joinToString()}")
            val startInterfaceOrientation = value.first().toUIInterfaceOrientation()
            val masks = value.map { it.toUIInterfaceOrientationMask() }
            var interfaceOrientationMask = masks.first()
            for (mask in masks) interfaceOrientationMask = interfaceOrientationMask or mask
            lockOrientation(startInterfaceOrientation, interfaceOrientationMask)
        }

    actual val currentOrientation: Orientation
        get() = UIDevice.currentDevice.orientation.toLayoutOrientation()

    /*internals*/

    private fun lockOrientation(startOrientation: UIInterfaceOrientation, orientationMask: UIInterfaceOrientationMask) {
        log("OrientationServiceDelegate.lockOrientation: $orientationMask")
        appDelegate.interfaceOrientationMask = orientationMask
        log("OrientationServiceDelegate.startOrientation: $startOrientation")
        UIDevice.currentDevice.setValue(startOrientation, forKey = "orientation")
        UINavigationController.attemptRotationToDeviceOrientation()
    }

}
