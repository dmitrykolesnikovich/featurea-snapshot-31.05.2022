@file:JvmName("ApplicationRuntime")

package featurea.app

import android.widget.RelativeLayout
import featurea.android.FeatureaActivity
import featurea.android.MainActivityContentViewProxy
import featurea.android.MainActivityProxy
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.Provide

actual fun ApplicationContainer() = Container {
    await(MainActivityProxy::class)
}

@Provide(MainActivityContentViewProxy::class)
actual fun ApplicationModule() = Module {
    onInit { appModule ->
        val mainActivity: FeatureaActivity = appModule.importComponent(MainActivityProxy)
        val mainView: RelativeLayout = appModule.createComponent("featurea.window.MainActivityContentView")
        appModule.provideComponent(MainActivityContentViewProxy(mainView))
        appModule.importComponent<Application>() // quickfix to import MainRender and MainView todo improve
        mainActivity.setContentView(mainView)
    }
}
