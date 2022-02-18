@file:JvmName("WindowRuntime")

package featurea.window

import featurea.breakpoint
import featurea.desktop.*
import featurea.runtime.Container
import featurea.runtime.Module
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javax.swing.JPanel
import javafx.application.Application as JfxApplication

actual fun WindowContainer() = Container {
    await(MainStageProxy::class)

    onInit {
        JfxApplication.launch(DesktopApplication::class.java)
    }

    onCreate { appContainer: Container ->
        val mainStage: Stage = appContainer.import(MainStageProxy)
        val root = StackPane()
        val scene = Scene(root, 600.0, 600.0)
        mainStage.scene = scene
        mainStage.show()
    }
}

actual fun WindowModule() = Module {
    onInit { appModule ->
        await(MainPanelProxy::class)
        await(MainNodeProxy::class)
        appModule.importComponent<Window>()
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
