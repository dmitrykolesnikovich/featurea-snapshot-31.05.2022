import UIKit
UIApplicationMain(CommandLine.argc, CommandLine.unsafeArgv, nil, NSStringFromClass(Bootstrap.self))

class Bootstrap : UIResponder, UIApplicationDelegate {
    
    var window: UIWindow? = UIWindow()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        window?.rootViewController = Sample1()
        window?.makeKeyAndVisible()
        return true
    }
    
}
