package featurea.ios.SlideMenuControllerSwift

import platform.UIKit.UIViewController

val UIViewController.slideMenuController: SlideMenuController?
    get() {
        var viewController: UIViewController? = this
        while (true) {
            println("slideMenuController #0: $viewController")
            if (viewController is SlideMenuController) return viewController
            if (viewController == null) return null
            viewController = viewController.parentViewController
        }
    }
