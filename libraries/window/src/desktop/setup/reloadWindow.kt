package featurea.window

import featurea.loader.clearCaches
import featurea.reload
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.container

fun Component.reloadWindow() {
    val windowModule: Module = container.modules["featurea.window.WindowModule"]
    container.clearCaches()
    container.reload(windowModule)
}
