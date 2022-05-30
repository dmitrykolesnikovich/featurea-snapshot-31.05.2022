package featurea.studio.project.menu

import featurea.desktop.jfx.fillWidth
import featurea.desktop.runOnJfxThread
import featurea.rml.deepCopy
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
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Stage
import javafx.stage.StageStyle

class SaveAsMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("File", "Save As...").apply {
                onAction = EventHandler {
                    val editor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    SaveAsDialog(editor).showAndWait()
                }
            }
        }
    }

    private inner class SaveAsDialog(editor: Editor) : Stage() {
        init {
            title = "Save As"
            val nameField = TextField()
            nameField.prefWidth = 400.0
            nameField.minWidth = 400.0
            nameField.text = editor.tab.id
            val okButton = Button("OK").apply {
                onAction = EventHandler {
                    val documentId = nameField.text
                    // 1. copy paste existing document
                    // >> todo encapsulate this
                    with(project.rmlResource) {
                        val copiedRmlTag = editor.tab.rmlResource.rmlTag.deepCopy()
                        copiedRmlTag.attributes["id"] = documentId
                        copiedRmlTag.attributes["name"] = documentId
                        rmlTag.children.add(copiedRmlTag)
                        rmlTag.properties[documentId] = copiedRmlTag
                    }
                    // 2. open newly created document
                    project.openDocument(documentId)
                    // <<
                    // >> quickfix todo improve
                    editor.tab.isDirty = true
                    editor.tab.save()
                    // <<
                    this@SaveAsDialog.close()
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
            isResizable = false

        }
    }

}

