package featurea.modbus.editor

import featurea.config.Config
import featurea.config.ConfigContent
import featurea.desktop.DesktopApplication
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FSMenuBar
import featurea.desktop.jfx.toImageOrNull
import featurea.runtime.*
import featurea.studio.ProjectMenuBarProxy
import featurea.studio.project.components.initProjectMenuBar
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import javafx.application.Application as JfxApplication

fun ModbusEditorRuntime(setup: ModuleBlock = {}) = Runtime {
    exportComponents(featurea.modbus.editor.artifact)
    injectContainer("featurea.modbus.editor.ModbusEditorContainer")
    injectModule("featurea.modbus.editor.ModbusEditorModule")
    complete(setup)
}

@Provide(ProjectMenuBarProxy::class)
fun ModbusEditorContainer() = Container {
    await(MainStageProxy::class)

    onInit {
        JfxApplication.launch(DesktopApplication::class.java)
    }
    onCreate { container ->
        runBlocking {
            val configContent: ConfigContent = container.import()
            val mainStage: Stage = container.import(MainStageProxy)

            provide(ProjectMenuBarProxy(container.initProjectMenuBar()))
            val buildConfig: Config = configContent.findConfig("build")
            val uiConfig: Config = configContent.findConfig("ui")
            mainStage.icons.add(uiConfig["studio.icon"]?.toImageOrNull())
            val version: String? = buildConfig["featurea.modbus.studio.version"]
            mainStage.title = "Channel Editor (version $version)" // todo make use of date
            mainStage.scene = Scene(BorderPane(), 960.0, 600.0)
            mainStage.show()
        }
    }
}

fun ModbusEditorModule() = Module {
    onInit { module ->
        module.importComponent<ModbusEditor>()
    }
}
