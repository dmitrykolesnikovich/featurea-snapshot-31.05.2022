package featurea.studio.project

import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.isEditing
import featurea.desktop.jfx.isShortcutKeyPressed
import featurea.runtime.Action
import featurea.runtime.import
import featurea.studio.editor.components.isMovable
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage

val moveSelectionWithArrows: Action = {
    val mainStage: Stage = import(MainStageProxy)
    val project: Project = import()

    mainStage.scene.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
        // filter
        if (isShortcutKeyPressed) return@addEventFilter
        val selectedEditor = project.findSelectedEditorOrNull() ?: return@addEventFilter
        if (selectedEditor.rmlTableView.isEditing()) return@addEventFilter
        if (selectedEditor.rmlTreeView.isFocused) return@addEventFilter
        val movableSelections = selectedEditor.selectionService.selections.filter { it.isMovable() }
        if (movableSelections.isEmpty()) return@addEventFilter

        // action
        when (event.code) {
            KeyCode.RIGHT -> {
                selectedEditor.selectionService.moveSelections(1f, 0f, false)
                event.consume()
            }
            KeyCode.LEFT -> {
                selectedEditor.selectionService.moveSelections(-1f, 0f, false)
                event.consume()
            }
            KeyCode.UP -> {
                selectedEditor.selectionService.moveSelections(0f, -1f, false)
                event.consume()
            }
            KeyCode.DOWN -> {
                selectedEditor.selectionService.moveSelections(0f, 1f, false)
                event.consume()
            }
        }
    }
}
