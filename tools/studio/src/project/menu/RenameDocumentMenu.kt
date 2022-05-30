package featurea.studio.project.menu

import featurea.config.Config
import featurea.desktop.jfx.closeOnEscape
import featurea.desktop.jfx.fillWidth
import featurea.desktop.runOnJfxThread
import featurea.rml.renameId
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Stage
import javafx.stage.StageStyle

class RenameDocumentMenu(override val module: Module) : Component {

    private val project: Project = import()
    private val uiConfig = Config("ui")

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("File", uiConfig["projectRenameDocumentDialogTitle", "Rename..."]).apply {
                onAction = EventHandler {
                    val editor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    RenameForm(editor).showAndWait()
                }
                accelerator = KeyCodeCombination(KeyCode.F6, KeyCombination.SHIFT_DOWN)
            }
        }
    }

    private inner class RenameForm(editor: Editor) : Stage() {
        init {
            title = uiConfig["projectRenameDocumentDialogTitle"]
            val nameField = TextField()
            nameField.prefWidth = 400.0
            nameField.minWidth = 400.0
            nameField.text = editor.tab.id
            val okButton = Button("OK").apply {
                onAction = EventHandler {
                    val newId = nameField.text
                    editor.tab.rmlResource.rmlTag.renameId(newId)
                    editor.tab.isDirty = true
                    editor.tab.save()
                    this@RenameForm.hide()
                }
                isDefaultButton = true
            }
            val contentPanel = GridPane().apply {
                padding = Insets(8.0)
                hgap = 8.0
                vgap = 8.0
                add(nameField.fillWidth(), 0, 0)
                add(okButton.fillWidth(HPos.RIGHT), 0, 1)
                columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
                rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
                rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
            }
            scene = Scene(contentPanel)
            initStyle(StageStyle.UTILITY)
            sizeToScene()
            closeOnEscape()
            isResizable = false
        }
    }

}
