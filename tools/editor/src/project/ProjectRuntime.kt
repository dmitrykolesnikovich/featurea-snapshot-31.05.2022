package featurea.studio.project

import featurea.config.Config
import featurea.config.ConfigContent
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FSMenuBar
import featurea.desktop.jfx.toImageOrNull
import featurea.desktop.runOnJfxThread
import featurea.log
import featurea.runtime.*
import featurea.studio.ProjectMenuBarProxy
import featurea.studio.project.components.initProjectMenuBar
import featurea.utils.runBlocking
import javafx.scene.Scene
import javafx.scene.control.MenuBar
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

internal var ProjectContainerCounter = 0

fun ProjectRuntime(studioModule: Module, setup: Action): Runtime {
    val studioContainer: Container = studioModule.container
    val components: Dependency = studioContainer.dependencyRegistry.artifact
    return Runtime(studioContainer.registry) {
        exportComponents(components)
        appendContainer("featurea.studio.project.ProjectContainer") {
            include(studioContainer)
        }
        injectModule("featurea.studio.project.ProjectModule") {
            include(studioModule)
        }
        complete(setup)
    }
}

@Provide(MainStageProxy::class)
@Provide(ProjectMenuBarProxy::class)
fun ProjectContainer() = Container {
    await(MainStageProxy::class)
    await(ProjectMenuBarProxy::class)

    onInit { projectContainer ->
        val mainStage: Stage = Stage()
        val mainStageProxy: MainStageProxy = MainStageProxy(mainStage)
        provide(mainStageProxy)
        val menuBar: FSMenuBar = projectContainer.initProjectMenuBar()
        provide(ProjectMenuBarProxy(menuBar))
    }
    onCreate { projectContainer ->
        val menuBar: MenuBar = projectContainer.import(ProjectMenuBarProxy)
        val mainStage: Stage = projectContainer.import(MainStageProxy)
        val rootPanel: BorderPane = BorderPane()
        menuBar.isUseSystemMenuBar = true
        rootPanel.top = menuBar
        mainStage.scene = Scene(rootPanel, 800.0, 600.0)
        runBlocking {
            val configContent: ConfigContent = projectContainer.import()
            val uiConfig: Config = configContent.findConfig("ui")
            val studioIcon: Image? = uiConfig["studio.icon"]?.toImageOrNull()
            mainStage.icons.add(studioIcon)
            mainStage.show()
            ProjectContainerCounter++
        }
    }
}

fun ProjectModule() = Module {
    onCreate { projectModule ->
        runOnJfxThread {
            projectModule.import<Project>()
        }
    }
}