package featurea.desktop.simulator.features

import featurea.desktop.MainStageProxy
import featurea.desktop.runOnJfxThread
import featurea.desktop.simulator.views.SimulatorMenuBar
import featurea.runtime.Action
import featurea.runtime.import
import javafx.event.EventHandler
import javafx.stage.Stage

val alwaysOnTop: Action = {
    val mainStage: Stage = import(MainStageProxy)
    val simulatorMenuBar: SimulatorMenuBar = import()

    runOnJfxThread {
        simulatorMenuBar.findCheckMenuItem("File", "Always on Top").apply {
            onAction = EventHandler {
                mainStage.isAlwaysOnTop = isSelected
            }
        }
    }
}
