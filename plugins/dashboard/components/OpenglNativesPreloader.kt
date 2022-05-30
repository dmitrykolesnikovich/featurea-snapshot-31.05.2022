package featurea.studio.home.components

import featurea.desktop.jfx.allFonts
import featurea.desktop.jfx.cyrillicFonts
import featurea.desktop.jfx.hiddenProgressBar
import featurea.desktop.jfx.toBackground
import featurea.desktop.jogamp.preloadOpenglNatives
import featurea.desktop.toJfxColor
import javafx.application.Preloader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.stage.StageStyle

class OpenglNativesPreloader : Preloader() {

    private lateinit var stage: Stage

    override fun start(primaryStage: Stage) {
        this.stage = primaryStage
        val contentPanel = GridPane()
        contentPanel.add(hiddenProgressBar(), 0, 0) // quickfix todo improve
        contentPanel.add(HBox().apply {
            this.minWidth = 400.0
            this.alignment = Pos.CENTER
            this.background = "#F6F6F6FF".toJfxColor().toBackground()
            children.add(Label("Loading..."))
        }, 0, 1)
        contentPanel.apply {
            columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.NEVER })
            rowConstraints.add(RowConstraints().apply { vgrow = Priority.NEVER })
            columnConstraints.add(ColumnConstraints().apply { hgrow = Priority.ALWAYS })
            rowConstraints.add(RowConstraints().apply { vgrow = Priority.ALWAYS })
        }
        stage.title = "Loading..."
        stage.scene = Scene(contentPanel, 400.0, 64.0)
        stage.initStyle(StageStyle.UNDECORATED)
        stage.sizeToScene()
        primaryStage.show()
    }

    override fun handleStateChangeNotification(stateChangeNotification: StateChangeNotification) {
        if (stateChangeNotification.type == StateChangeNotification.Type.BEFORE_START) {
            preloadOpenglNatives()
            allFonts // eager load of allFonts
            cyrillicFonts // eager load of cyrillicFonts
            stage.hide()
        }
    }

}
