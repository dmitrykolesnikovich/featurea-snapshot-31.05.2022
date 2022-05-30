package featurea.studio.project.menu

import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.SelectionService
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class ClearValueMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Edit", "Clear").apply {
                onAction = EventHandler {
                    val selectedEditor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    val selectionService = selectedEditor.import<SelectionService>()
                    selectionService.clearSelectedValue()
                }
                accelerator = KeyCodeCombination(KeyCode.F3)
            }
        }
    }

}
