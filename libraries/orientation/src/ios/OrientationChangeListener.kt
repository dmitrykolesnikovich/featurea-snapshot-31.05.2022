package featurea.orientation

import featurea.ios.UIApplicationDelegate
import featurea.ios.UIApplicationProxy
import featurea.ios.UIViewControllerListener
import featurea.layout.Orientation
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.window.Window
import platform.CoreGraphics.CGSize
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController

class OrientationChangeListener(override val module: Module) : Component, UIViewControllerListener {

    private val appDelegate: UIApplicationDelegate = module.importComponent(UIApplicationProxy)
    private val window: Window = import()

    override fun onCreateComponent() {
        appDelegate.viewControllerListeners.add(this)
    }

    override fun onDeleteComponent() {
        appDelegate.viewControllerListeners.remove(this)
    }

    override fun viewWillTransitionToSize(viewController: UIViewController, size: CGSize) {
        val orientation: Orientation = UIDevice.currentDevice.orientation.toLayoutOrientation()
        window.updateOrientation(orientation)
    }

}
