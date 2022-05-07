package featurea.window

import featurea.loader.clearCaches
import featurea.utils.reload
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.container

fun Component.reloadApplication() {
    val appModule: Module = container.modules["featurea.ApplicationModule"]
    container.clearCaches()
    container.reload(appModule)
}
