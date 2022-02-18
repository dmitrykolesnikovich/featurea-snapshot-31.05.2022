package featurea.studio.project.menu

import featurea.config.Config
import featurea.content.ResourceTag
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FileChooser
import featurea.desktop.jfx.closeOnEscape
import featurea.desktop.jfx.fillWidth
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.home.StudioDelegate
import featurea.studio.home.StudioPanel
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
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File

class NewDocumentProjectMenu(override val module: Module) : Component {

    private val mainStage: Stage = import(MainStageProxy)
    private val project: Project = import()
    private val studioPanel: StudioPanel = import()

    private val uiConfig = Config("ui")

    init {
        project.menuBar.findMenuItem("File", "New Project..."). onAction = EventHandler {
            val delegate: StudioDelegate = studioPanel.options.delegate
            val fileChooser: FileChooser = FileChooser(ExtensionFilter("Project files (*.project)", "*.project"))
            val file: File? = fileChooser.showSaveDialog(mainStage)
            if (file != null) {
                delegate.newProject(file)
                delegate.openProject(file)
            }
        }
        project.menuBar.findMenuItem("File", uiConfig["projectNewDocumentTitle", "New..."]).onAction = EventHandler {
            NewDocumentDialog { documentId ->
                val rmlTag = project.delegate.newDocument(documentId)
                project.openDocument(documentId)
                rmlTag
            }.showAndWait()
        }
    }

    private inner class NewDocumentDialog(createRmlTag: (name: String) -> ResourceTag) : Stage() {
        init {
            title = uiConfig["projectNewDocumentTitle"]?.removeSuffix("...")
            val nameField = TextField().apply { prefWidth = 400.0; minWidth = 400.0 }
            val okButton = Button("OK").apply {
                onAction = EventHandler {
                    val name = nameField.text
                    if (project.rmlResource.rmlTag.properties.containsKey(name)) return@EventHandler
                    val rmlTag = createRmlTag(name)
                    project.save(rmlTag)
                    this@NewDocumentDialog.close()
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
