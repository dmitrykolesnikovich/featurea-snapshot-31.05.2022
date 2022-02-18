package featurea.desktop.simulator

import featurea.desktop.DesktopApplication
import featurea.desktop.MainStageProxy
import featurea.desktop.simulator.features.alwaysOnTop
import featurea.desktop.simulator.features.openBundle
import featurea.desktop.simulator.features.uploadBundle
import featurea.desktop.simulator.types.Simulator
import featurea.desktop.simulator.views.SimulatorMenuBar
import featurea.desktop.simulator.views.SimulatorTabPanel
import featurea.runtime.*
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

/*dependencies*/

val artifact = Artifact("featurea.desktop.simulator") {
    "Simulator" to ::Simulator
    "SimulatorContainer" to ::SimulatorContainer
    "SimulatorModule" to ::SimulatorModule
    "SimulatorMenuBar" to ::SimulatorMenuBar
    "SimulatorTabPanel" to ::SimulatorTabPanel

    SimulatorPlugin {
        "alwaysOnTopMenu" to alwaysOnTop
        "openBundleMenu" to openBundle
        "uploadBundle" to uploadBundle
    }
}

fun DependencyBuilder.SimulatorPlugin(plugin: Plugin<Simulator>) = install(plugin)

/*runtime*/

fun SimulatorContainer() = Container {
    await(MainStageProxy::class)

    onInit {
        Application.launch(DesktopApplication::class.java)
    }

    onCreate { simulatorContainer ->
        val mainStage: Stage = simulatorContainer.import(MainStageProxy)
        mainStage.scene = Scene(BorderPane(), 600.0, 600.0)
        mainStage.show()
    }
}

fun SimulatorModule() = Module {
    onInit { simulatorModule ->
        simulatorModule.importComponent<Simulator>()
    }
}
