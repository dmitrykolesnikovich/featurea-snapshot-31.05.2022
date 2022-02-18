package featurea.modbus.editor

import featurea.System
import featurea.runCommand
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.*
import featurea.desktop.runOnJfxThread
import featurea.joinToStringWithQuotes
import featurea.jvm.normalizedPath
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.withExtension
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
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
import kotlinx.coroutines.runBlocking

// quickfix todo avoid reference to `scada` tool
class SaveStationMenu(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val modbusEditor: ModbusEditor = import()
    private val modbusEditorMenuBar: ModbusEditorMenuBar = import()
    private val system: System = import()

    private val bundleFileTextField = TextField()
    private val progressBarDialog = FSProgressBarDialog("Save Station")
    private val projectFileTextField = TextField()
    private val stage = Stage(mainStage).registerGlobalKeyEvents()

    init {
        runOnJfxThread {
            modbusEditorMenuBar.findMenuItem("File", "Save Station...").apply {
                onAction = EventHandler {
                    val document = modbusEditor.selectedDocument ?: return@EventHandler

                    if (document.editor.tab.isDirty) {
                        confirmDialog("Save Station", "Save document before creating new station?") {
                            document.save()
                        }
                    }
                    projectFileTextField.text = document.editor.project.rmlFile.normalizedPath
                    bundleFileTextField.text = document.editor.project.rmlFile.normalizedPath.withExtension("station")
                    stage.show()
                }
                accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
        }
    }

    init {
        stage.title = "Save Station"
        val contentPanel = GridPane().apply {
            padding = Insets(8.0)
            hgap = 8.0
            vgap = 8.0
        }
        contentPanel.add(projectFileTextField.withLabel("Project File"), 0, 0)
        contentPanel.add(Button("..."), 1, 0)
        contentPanel.add(bundleFileTextField.withLabel("Bundle File"), 0, 1)
        contentPanel.add(Button("..."), 1, 1)
        contentPanel.add(Button("OK").apply {
            isDefaultButton = true
            onAction = EventHandler {
                val projectFile = projectFileTextField.text ?: return@EventHandler
                val bundleFile = bundleFileTextField.text ?: return@EventHandler
                stage.close()
                progressBarDialog.showAndWait()
                runBlocking {
                    val roots = system.contentRoots.joinToStringWithQuotes()
                    val exitCode = runCommand("scada station '$projectFile' '$bundleFile' $roots", name = "Station") {
                        if (it.startsWith("progress: ")) {
                            progressBarDialog.updateProgress(it.removePrefix("progress: ").toDouble())
                        }
                    }
                    progressBarDialog.close()
                    if (exitCode != 0) {
                        runOnJfxThread {
                            warningDialog("Save Station", "Error")
                        }
                    } else {
                        runOnJfxThread {
                            infoDialog("Save Station", "Finished successfully")
                        }
                    }
                }
            }
        }.fillWidth(HPos.RIGHT), 1, 2)
        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS; valignment = VPos.BOTTOM })
        contentPanel.minWidth = 960.0
        stage.scene = Scene(contentPanel)
        stage.closeOnEscape()
        stage.initStyle(StageStyle.UTILITY)
        stage.sizeToScene()
    }

}
