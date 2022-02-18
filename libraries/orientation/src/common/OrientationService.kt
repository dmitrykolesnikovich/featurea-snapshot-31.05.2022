package featurea.orientation

import featurea.layout.Orientation
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.window.Window
import featurea.window.WindowListener

class OrientationService(override val module: Module) : Component, WindowListener {

    private val delegate: OrientationServiceDelegate = import()
    private val window: Window = import()

    var allowedOrientations: Collection<Orientation>
        set(value) {
            delegate.allowedOrientations = value
        }
        get() {
            return delegate.allowedOrientations
        }
    val currentOrientation: Orientation get() = delegate.currentOrientation

    init {
        window.listeners.add(this)
    }

    override fun resize(width: Int, height: Int) {
        window.updateOrientation(currentOrientation)
    }

    fun isHorizontalOrientationAllowed(): Boolean = delegate.allowedOrientations.contains(Orientation.LandscapeLeft) ||
            delegate.allowedOrientations.contains(Orientation.LandscapeRight)

    fun isVerticalOrientationAllowed(): Boolean = delegate.allowedOrientations.contains(Orientation.Portrait) ||
            delegate.allowedOrientations.contains(Orientation.PortraitUpsideDown)

}
