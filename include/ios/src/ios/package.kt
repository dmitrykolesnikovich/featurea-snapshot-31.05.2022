package featurea.ios

import featurea.runtime.Artifact
import featurea.runtime.Delegate
import featurea.runtime.Proxy
import platform.GLKit.GLKView
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

/*dependencies*/

val artifact = Artifact("featurea.ios") {
    "MainControllerProxy" to MainControllerProxy::class
    "MainViewProxy" to MainViewProxy::class
    "MainWindowProxy" to MainWindowProxy::class
    "NavigationControllerProxy" to NavigationControllerProxy::class
    "SimulatorControllerProxy" to SimulatorControllerProxy::class
    "UIApplicationProxy" to UIApplicationProxy::class
}

class MainControllerProxy(override val delegate: UIViewController) : Proxy<UIViewController> {
    companion object : Delegate<UIViewController>(MainControllerProxy::class)
}
class MainViewProxy(override val delegate: GLKView) : Proxy<GLKView> {
    companion object : Delegate<GLKView>(MainViewProxy::class)
}
class MainWindowProxy(override val delegate: UIWindow) : Proxy<UIWindow> {
    companion object : Delegate<UIWindow>(MainWindowProxy::class)
}
class NavigationControllerProxy(override var delegate: UINavigationController) : Proxy<UINavigationController> {
    companion object : Delegate<UINavigationController>(NavigationControllerProxy::class)
}
class SimulatorControllerProxy(override val delegate: UIViewController) : Proxy<UIViewController> {
    companion object : Delegate<UIViewController>(SimulatorControllerProxy::class)
}
class UIApplicationProxy(override val delegate: UIApplicationDelegate) : Proxy<UIApplicationDelegate> {
    companion object : Delegate<UIApplicationDelegate>(UIApplicationProxy::class)
}
