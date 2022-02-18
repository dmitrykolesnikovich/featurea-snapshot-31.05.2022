package featurea.desktop.simulator.types

import featurea.desktop.simulator.views.SimulatorTabPanel
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import javafx.scene.control.Tab

class Simulator(override val module: Module) : Component {
    val tabPanel: SimulatorTabPanel = import()
    val tabModules = mutableMapOf<Tab, Module>()
    val moduleTabs = mutableMapOf<Module, Tab>()
}
