@file:JvmName("WindowRuntime")

package featurea.window

import featurea.Application
import featurea.android.MainActivityContentViewProxy
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.Provide

actual fun WindowContainer() = Container {
    await(MainActivityProxy::class)
}

@Provide(MainActivityContentViewProxy::class)
actual fun WindowModule() = Module {
    onInit { appModule ->
        val mainActivity: FeatureaActivity = appModule.importComponent(MainActivityProxy)
        val mainActivityContentView: MainActivityContentView = appModule.createComponent()
        appModule.provideComponent(MainActivityContentViewProxy(mainActivityContentView))
        appModule.importComponent<Application>() // quickfix to import featurea.window.MainRender and featurea.input.MainView todo improve
        mainActivity.setContentView(mainActivityContentView)
    }
}
