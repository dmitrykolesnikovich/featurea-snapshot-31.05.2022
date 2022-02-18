package featurea.desktop.simulator.views

import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FSMenuBar
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import javafx.scene.layout.BorderPane

class SimulatorMenuBar(override val module: Module) : Component, FSMenuBar() {

    private val rootPane: BorderPane = import(MainStageProxy).scene.root as BorderPane

    init {
        rootPane.top = this
    }

}
