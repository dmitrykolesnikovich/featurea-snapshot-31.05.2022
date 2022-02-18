package featurea.desktop.simulator.views

import featurea.desktop.MainStageProxy
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import javafx.scene.control.TabPane
import javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS
import javafx.scene.layout.BorderPane

class SimulatorTabPanel(override val module: Module) : Component, TabPane() {

    private val rootPanel = import(MainStageProxy).scene.root as BorderPane

    init {
        tabClosingPolicy = ALL_TABS
        rootPanel.center = this
    }

}