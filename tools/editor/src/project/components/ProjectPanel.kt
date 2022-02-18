package featurea.studio.project.components

import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.closeOnEscape
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.components.DocumentListView
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Stage
import javafx.stage.StageStyle

class ProjectPanel(override val module: Module) : Component {

    private val project: Project = import()
    private val mainStage: Stage = import(MainStageProxy)

    private val listView = DocumentListView()
    private val contentPanel = GridPane()
    private val openButton = Button("Open").apply { onAction = EventHandler { open() }; isDefaultButton = true }
    private val stage = Stage()

    init {
        listView.onMouseClicked = EventHandler { event ->
            if (event.clickCount == 2) {
                open()
            }
        }
        /*listView.selectionModel.selectionMode = SelectionMode.MULTIPLE*/

        contentPanel.apply {
            padding = Insets(8.0)
            hgap = 8.0
            vgap = 8.0

            add(listView, 0, 0)
            /*add(openButton.fillWidth(), 0, 1)*/

            columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.SOMETIMES })
            rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS })
            /*rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })*/
        }
        stage.apply {
            initStyle(StageStyle.UTILITY)
            initOwner(mainStage)
            closeOnEscape()
            scene = Scene(contentPanel, 200.0, 400.0)
        }
    }

    fun show(name: String) {
        stage.title = "Open $name"
        listView.update(project.rmlResource, name)
        stage.showAndWait()
    }

    private fun open() {
        for (item in listView.selectionModel.selectedItems) {
            project.openDocument(item)
        }
        stage.hide()
    }

}
