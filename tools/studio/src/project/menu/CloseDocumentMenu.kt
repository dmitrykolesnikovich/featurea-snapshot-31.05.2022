package featurea.studio.project.menu

import featurea.desktop.runOnJfxThread
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import featurea.studio.project.Project
import featurea.studio.project.components.ProjectTabPanel
import featurea.config.Config
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class CloseDocumentMenu(override val module: Module) : Component {

    private val project: Project = import()
    private val tabPanel: ProjectTabPanel = import()
    private val uiConfig = Config("ui")

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("File", uiConfig["closeDocumentScriptMenuItem", "Close Editor"]).apply {
                onAction = EventHandler { project.closeDocument(tabPanel.selectionModel.selectedItem) }
                accelerator = KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }

}
