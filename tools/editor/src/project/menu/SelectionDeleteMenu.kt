package featurea.studio.project.menu

import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.confirmDialog
import featurea.desktop.jfx.isEditing
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.stage.Stage

class SelectionDeleteMenu(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Edit", "Delete").apply {
                onAction = EventHandler {
                    if (!mainStage.isFocused) return@EventHandler
                    val editor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    if (editor.rmlTableView.isEditing()) return@EventHandler

                    val selectedRmlTags = editor.selectionService.selectedRmlTags
                    confirmDialog(
                        "Delete",
                        "Delete ${selectedRmlTags.joinToString { it.attributes["name"] ?: it.name }}?"
                    ) {
                        editor.remove(*selectedRmlTags.toTypedArray())
                        editor.selectionService.select(editor.tab.rmlResource.rmlTag)
                    }
                }
                accelerator = KeyCodeCombination(KeyCode.DELETE)
            }
        }
    }

}
