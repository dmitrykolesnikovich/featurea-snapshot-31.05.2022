package featurea.app

import featurea.ios.MainControllerProxy
import featurea.ios.MainViewProxy
import featurea.ios.MainWindowProxy
import featurea.ios.UIApplicationProxy
import featurea.utils.log
import featurea.runtime.Container
import featurea.runtime.Module
import kotlin.native.concurrent.freeze

actual fun ApplicationContainer() = Container {
    await(UIApplicationProxy::class)
    await(MainWindowProxy::class)
    onInit {
        setUnhandledExceptionHook({ e: Throwable ->
            initRuntimeIfNeeded()
            log("UnhandledException: ${e.message}")
            e.printStackTrace()
        }.freeze())
    }
}

actual fun ApplicationModule() = Module {
    onInit { appModule ->
        await(MainControllerProxy::class)
        await(MainViewProxy::class)
        appModule.importComponent("featurea.window.Window")
    }
    onCreate { appModule ->
        val mainController = appModule.importComponent(MainControllerProxy)
        val mainWindow = appModule.importComponent(MainWindowProxy)
        mainWindow.rootViewController = mainController
        mainWindow.makeKeyAndVisible()
    }
}
