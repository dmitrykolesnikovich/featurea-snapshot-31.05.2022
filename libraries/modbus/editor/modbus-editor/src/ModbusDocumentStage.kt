package featurea.modbus.editor

import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.confirmDialog
import featurea.desktop.jfx.fillWidth
import featurea.desktop.jfx.isShortcutKeyPressed
import featurea.utils.watchOnJfxThread
import featurea.jvm.normalizedPath
import featurea.runtime.*
import featurea.script.Script
import featurea.studio.editor.components.EditorTab
import featurea.studio.project.Project
import featurea.utils.runOnEditorThread
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File

class ModbusDocumentStage(override val module: Module) : Component {

    private val editorTab: EditorTab = import()
    private val mainStage: Stage = import(MainStageProxy)
    private val modbusDocument: ModbusDocument = import()
    private val project: Project = import()

    private val contentPanel = GridPane()

    init {
        mainStage.title = "Channel Editor"
        mainStage.scene = Scene(contentPanel, 960.0, 400.0)
        mainStage.initStyle(StageStyle.UTILITY)
        mainStage.scene.onKeyPressed = EventHandler { event ->
            if (isShortcutKeyPressed && event.code == KeyCode.S) {
                modbusDocument.editorTab.save()
            }
        }
        mainStage.onCloseRequest = EventHandler {
            if (editorTab.isDirty) {
                confirmDialog("Save", "Save ${editorTab.id}?") { editorTab.save() }
            }
        }
    }

    init {
        contentPanel.apply {
            padding = Insets(8.0)
            hgap = 8.0
            vgap = 8.0
            add(BorderPane().apply {
                top = import<ModbusDocumentToolbar>()
                center = modbusDocument.panel
            }, 0, 0)
            add(Button("OK").apply {
                isDefaultButton = true;
                onAction = EventHandler {
                    if (editorTab.isDirty) {
                        confirmDialog("Save", "Save ${editorTab.id}?") { editorTab.save() }
                    }
                    mainStage.hide()
                }
            }.fillWidth(HPos.RIGHT), 0, 1)
            columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
            rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS })
            rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        }
    }

    init {
        editorTab.isDirtyProperty.watchOnJfxThread {
            if (editorTab.isDirty) {
                project.mainStage.title = "* ${project.rmlFile.normalizedPath}"
            } else {
                project.mainStage.title = project.rmlFile.normalizedPath
            }
        }
    }

    fun show(file: File) = runOnEditorThread {
        modbusDocument.initFile(file)
        mainStage.show()
    }

}
