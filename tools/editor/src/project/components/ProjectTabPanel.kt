package featurea.studio.project.components

import featurea.desktop.MainStageProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import javafx.scene.Parent
import javafx.scene.control.TabPane
import javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class ProjectTabPanel(override val module: Module) : Component, TabPane() {

    private val mainStage: Stage = import(MainStageProxy)

    private val rootPanel: BorderPane?
        get() { // quickfix todo decouple project from ProjectPlugin -> features -> projectMenuBar
            val root: Parent? = mainStage.scene.root
            return if (root is BorderPane) root else null
        }

    init {
        tabClosingPolicy = ALL_TABS
        rootPanel?.center = this
    }

}
