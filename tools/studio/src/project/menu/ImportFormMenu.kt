package featurea.studio.project.menu

import featurea.config.Config
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.fillWidth
import featurea.desktop.jfx.registerGlobalKeyEvents
import featurea.desktop.runOnJfxThread
import featurea.jvm.normalizedPath
import featurea.rml.reader.RmlContent
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.DocumentListView
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.runBlocking
import featurea.desktop.jfx.Stage

class ImportFormMenu(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val project: Project = import()
    private val rmlContent: RmlContent = import()

    private val uiConfig: Config = Config("ui")
    private val listView: DocumentListView = DocumentListView()
    private val stage: Stage = Stage(mainStage).registerGlobalKeyEvents()

    init {
        val okButton = Button("OK").apply { isDefaultButton = true }
        stage.title = uiConfig["projectImportDocumentTitle"]
        val contentPanel = GridPane().apply {
            padding = Insets(8.0)
            hgap = 8.0
            vgap = 8.0
        }
        stage.scene = Scene(contentPanel, 200.0, 400.0)
        stage.initStyle(StageStyle.UTILITY)
        stage.isResizable = true
        contentPanel.add(listView.fillWidth(), 0, 0)
        contentPanel.add(okButton.fillWidth(HPos.RIGHT), 0, 1)
        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
        listView.onMouseClicked = EventHandler { event ->
            if (event.clickCount == 2) {
                stage.hide()
            }
        }
        okButton.onAction = EventHandler {
            stage.hide()
        }
        runOnJfxThread {
            project.menuBar.findMenuItem("File", uiConfig["projectImportDocumentTitle", "Import..."]).apply {
                onAction = EventHandler {
                    val file = FileChooser().apply {
                        extensionFilters.add(FileChooser.ExtensionFilter("Project files (*.project)", "*.project"))
                    }.showOpenDialog(mainStage)
                    if (file != null) {
                        runBlocking {
                            val rmlFile = rmlContent.readRmlFile(file.normalizedPath) ?: return@runBlocking
                            listView.update(rmlFile.rmlTag)
                            stage.showAndWait()
                            val documentRmlTag =
                                rmlFile.rmlTag.properties[listView.selectionModel.selectedItem] ?: return@runBlocking
                            project.importRmlTag(documentRmlTag)
                        }
                    }
                }
            }
        }
    }

}
