package featurea.desktop.jfx

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.ProgressBar
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window

class FSProgressBarDialog(title: String, mainStage: Window? = null) {

    val stage = Stage().registerGlobalKeyEvents()
    private var progressBar = ProgressBar()

    init {
        val contentPanel = GridPane().apply {
            padding = Insets(4.0)
            hgap = 4.0
            vgap = 4.0
        }
        progressBar.maxWidth = 1000.0
        contentPanel.add(progressBar, 0, 0)
        contentPanel.columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
        contentPanel.rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS })
        stage.title = title
        stage.scene = Scene(contentPanel, 300.0, 30.0)
        stage.initStyle(StageStyle.UTILITY)
        stage.initOwner(mainStage)
        stage.sizeToScene()
    }

    fun showAndWait() = Platform.runLater {
        progressBar.progress = -1.0
        stage.showAndWait()
    }

    fun show() = Platform.runLater {
        progressBar.progress = -1.0
        stage.show()
    }

    fun close() = Platform.runLater {
        progressBar.progress = 1.0
        stage.close()
    }

    fun updateProgress(progress: Double) = Platform.runLater {
        progressBar.progress = progress
    }

}
