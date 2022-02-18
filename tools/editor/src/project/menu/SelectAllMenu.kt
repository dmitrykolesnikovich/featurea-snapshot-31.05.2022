package featurea.studio.project.menu

import featurea.desktop.MainStageProxy
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.SelectionService
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN
import javafx.stage.Stage

class SelectAllMenu(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Edit", "Select All").apply {
                accelerator = KeyCodeCombination(KeyCode.A, SHORTCUT_DOWN)
                onAction = EventHandler {
                    if (!mainStage.isFocused) return@EventHandler
                    val editor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    val selectionService = editor.import<SelectionService>()

                    selectionService.selectAll()
                }
            }
        }
    }

}
