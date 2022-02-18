package featurea.orientation

import featurea.layout.Orientation
import platform.UIKit.*
import platform.UIKit.UIDeviceOrientation.*

fun UIDeviceOrientation.toLayoutOrientation(): Orientation = when (this) {
    UIDeviceOrientationLandscapeLeft -> Orientation.LandscapeLeft
    UIDeviceOrientationLandscapeRight -> Orientation.LandscapeRight
    UIDeviceOrientationPortrait -> Orientation.Portrait
    UIDeviceOrientationPortraitUpsideDown -> Orientation.PortraitUpsideDown
    UIDeviceOrientationFaceUp -> Orientation.Portrait // quickfix todo improve
    UIDeviceOrientationFaceDown -> Orientation.PortraitUpsideDown // quickfix todo improve
    else -> error("orientation: $this")
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Orientation.toUIInterfaceOrientationMask(): UIInterfaceOrientationMask = when (this) {
    Orientation.Portrait -> UIInterfaceOrientationMaskPortrait
    Orientation.PortraitUpsideDown -> UIInterfaceOrientationMaskPortraitUpsideDown
    Orientation.LandscapeRight -> UIInterfaceOrientationMaskLandscapeRight
    Orientation.LandscapeLeft -> UIInterfaceOrientationMaskLandscapeLeft
}

fun Orientation.toUIInterfaceOrientation(): UIInterfaceOrientation = when (this) {
    Orientation.Portrait -> UIInterfaceOrientationPortrait
    Orientation.PortraitUpsideDown -> UIInterfaceOrientationPortraitUpsideDown
    Orientation.LandscapeRight -> UIInterfaceOrientationLandscapeRight
    Orientation.LandscapeLeft -> UIInterfaceOrientationLandscapeLeft
}
