@file:JvmName("WindowRuntime")

package featurea.window

import android.widget.RelativeLayout
import featurea.Application
import featurea.android.FeatureaActivity
import featurea.android.MainActivityContentViewProxy
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

        // >>
        // 1)
        // val mainActivityContentView: MainActivityContentView = appModule.createComponent()
        // 2)
        val mainActivityContentView: RelativeLayout = TODO("appModule.createComponent()")
        // <<

        appModule.provideComponent(MainActivityContentViewProxy(mainActivityContentView))
        appModule.importComponent<Application>() // quickfix to import featurea.window.MainRender and featurea.input.MainView todo improve
        mainActivity.setContentView(mainActivityContentView)
    }
}
