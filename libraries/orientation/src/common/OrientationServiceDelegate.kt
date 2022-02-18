package featurea.orientation

import featurea.layout.Orientation
import featurea.runtime.Component
import featurea.runtime.Module

internal expect class OrientationServiceDelegate(module: Module) : Component {
    var allowedOrientations: Collection<Orientation>
    val currentOrientation: Orientation
}
