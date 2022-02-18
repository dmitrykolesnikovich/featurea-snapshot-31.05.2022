package featurea.studio.home

import com.sun.javafx.application.LauncherImpl
import featurea.config.importConfig
import featurea.desktop.DesktopApplication
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.toImageOrNull
import featurea.runtime.*
import featurea.studio.home.components.OpenglNativesPreloader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

fun bootstrap(artifact: Dependency, setup: Action = {}) = Runtime {
    exportComponents(artifact)
    injectContainer("featurea.studio.StudioContainer")
    injectModule("featurea.studio.StudioModule")
    complete(setup)
}

fun StudioContainer() = Container {
    await(MainStageProxy::class)

    onInit {
        LauncherImpl.launchApplication(DesktopApplication::class.java, OpenglNativesPreloader::class.java, emptyArray())
    }
}

fun StudioModule() = Module {
    onCreate { studioModule ->
        val mainStage: Stage = studioModule.importComponent(MainStageProxy)
        val uiConfig = studioModule.importConfig("ui")
        mainStage.apply {
            uiConfig["studio.icon"]?.toImageOrNull()?.also { icons.add(it) }
            title = uiConfig["studio.title"]
            isResizable = false
            scene = Scene(Pane(), 600.0, 300.0)
            sizeToScene()
        }
    }
}
