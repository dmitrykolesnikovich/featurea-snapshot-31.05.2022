package featurea.orientation

import featurea.layout.AllOrientations
import featurea.layout.Orientation
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.window.Window

internal actual class OrientationServiceDelegate actual constructor(override val module: Module) : Component {

    private val window: Window = import()

    actual var allowedOrientations: Collection<Orientation> = AllOrientations

    actual val currentOrientation: Orientation
        get() = if (window.surface.size.width > window.surface.size.height) {
            Orientation.LandscapeRight
        } else {
            Orientation.Portrait
        }

}
