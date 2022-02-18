package featurea.window

import featurea.Application
import featurea.ios.*
import featurea.runtime.Module
import featurea.runtime.Provide
import featurea.runtime.provide
import kotlinx.cinterop.CValue
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.GLKit.GLKView
import platform.UIKit.*

private typealias WithTransitionCoordinator = UIViewControllerTransitionCoordinatorProtocol

@Provide(MainControllerProxy::class)
class MainController(module: Module) : UIViewController(null, null) {

    private val app: Application = module.importComponent()
    private val appDelegate: UIApplicationDelegate = module.importComponent(UIApplicationProxy)
    private val mainController: MainController = this
    private val mainView: GLKView = module.importComponent(MainViewProxy)
    private val window: Window = module.importComponent()

    init {
        mainView.setFrame(UIScreen.mainScreen.bounds)
        view.addSubview(mainView)
        window.init()
        val size: CGSize = mainView.frame.value.size
        resize(size)
        provide(MainControllerProxy(mainController))
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        app.onCreate()
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        app.onStart()
        app.onResume()
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        app.onPause()
        app.onStop()
    }

    override fun viewDidUnload() {
        super.viewDidUnload()
        app.onDestroy()
    }

    override fun viewWillTransitionToSize(size: CValue<CGSize>, withTransitionCoordinator: WithTransitionCoordinator) {
        super.viewWillTransitionToSize(size, withTransitionCoordinator)
        val size: CGSize = size.value
        mainView.setFrame(CGRectMake(0.0, 0.0, size.width, size.height))
        resize(size)
        for (listener in appDelegate.viewControllerListeners) {
            listener.viewWillTransitionToSize(mainController, size)
        }
    }

    /*internals*/

    private fun resize(size: CGSize) {
        val widthInPixels: Int = size.width.toInt() * pixelsInPoint.toInt()
        val heightInPixels: Int = size.height.toInt() * pixelsInPoint.toInt()
        window.resize(widthInPixels, heightInPixels)
    }

}
