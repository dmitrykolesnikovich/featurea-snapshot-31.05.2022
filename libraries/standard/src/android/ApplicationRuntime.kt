@file:JvmName("ApplicationRuntime")

package featurea

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
        val mainView: RelativeLayout = appModule.importComponent(MainActivityContentViewProxy)
        appModule.importComponent<Application>() // quickfix to import featurea.window.MainRender and featurea.input.MainView todo improve
        mainActivity.setContentView(mainView)
    }
}
