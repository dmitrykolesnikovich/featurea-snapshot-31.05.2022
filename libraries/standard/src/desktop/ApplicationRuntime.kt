@file:JvmName("ApplicationRuntime")

package featurea

import featurea.desktop.*
import featurea.runtime.Container
import featurea.runtime.Module
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javax.swing.JPanel
import javafx.application.Application as JfxApplication

actual fun ApplicationContainer() = Container {
    await(MainStageProxy::class)

    onInit {
        JfxApplication.launch(DesktopApplication::class.java)
    }

    onCreate { appContainer: Container ->
        val mainStage: Stage = appContainer.import(MainStageProxy)
        val rootPanel: Pane = StackPane()
        val scene: Scene = Scene(rootPanel, 600.0, 600.0)
        mainStage.scene = scene
        mainStage.show()
    }
}

actual fun ApplicationModule() = Module {
    onInit { appModule ->
        await(MainPanelProxy::class)
        await(MainNodeProxy::class)
        appModule.importComponent("featurea.window.Window")
    }

    onCreate { appModule ->
        val mainPanel: JPanel = appModule.importComponent(MainPanelProxy)
        val mainStage: Stage = appModule.importComponent(MainStageProxy)
        val rootPanel: Pane = mainStage.scene.root as Pane
        rootPanel.children.add(SwingNode(mainPanel))
    }

    onDestroy { appModule ->
        val mainStage: Stage = appModule.importComponent(MainStageProxy)
        val rootPanel: Pane = mainStage.scene.root as Pane
        rootPanel.children.clear()
    }
}
