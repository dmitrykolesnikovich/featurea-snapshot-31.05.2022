package featurea.desktop.simulator.features

import featurea.desktop.MainNodeProxy
import featurea.desktop.MainStageProxy
import featurea.desktop.runOnJfxThread
import featurea.desktop.simulator.types.Simulator
import featurea.desktop.simulator.views.SimulatorMenuBar
import featurea.jvm.normalizedPath
import featurea.loader.Loader
import featurea.log
import featurea.runtime.*
import featurea.utils.runOnApplicationThread
import featurea.window.Window
import javafx.embed.swing.SwingNode
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

val openBundle: Action = {
    val mainStage: Stage = import(MainStageProxy)
    val simulator: Simulator = import()
    val simulatorMenuBar: SimulatorMenuBar = import()

    runOnJfxThread {
        simulatorMenuBar.findMenuItem("File", "Open...").apply {
            onAction = EventHandler {
                val fileChooser = FileChooser()
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Bundle files (*.bundle)", "*.bundle"))
                val file: File = fileChooser.showOpenDialog(mainStage) ?: return@EventHandler
                if (file.exists()) {
                    simulator.openBundle(file)
                }
            }
            accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
        }
    }
}

fun Simulator.openBundle(file: File) {
    if (!file.exists()) return

    runOnJfxThread {
        val bundlePath: String = file.normalizedPath
        defaultProxyScope {
            exportComponents(module.container.artifact)
            appendDefaultContainer {
                include(module.container)
            }
            injectDefaultModule()
            init { appModule ->
                appModule.importComponent<Window>()
            }
            complete { appModule ->
                val loader: Loader = appModule.importComponent()
                val mainNode: Node = appModule.importComponent(MainNodeProxy)

                runOnApplicationThread {
                    val tab = Tab().apply {
                        onCloseRequest = EventHandler {
                            appModule.destroy()
                        }
                        text = file.name
                        content = mainNode
                    }
                    loader.loadBundle(bundlePath)
                    runOnJfxThread {
                        tabPanel.tabs.add(tab)
                        tabPanel.selectionModel.select(tab)
                        // >> IMPORTANT
                        mainNode as SwingNode
                        mainNode.content.requestFocus()
                        mainNode.content.requestFocusInWindow()
                    }
                    moduleTabs[appModule] = tab
                    tabModules[tab] = appModule
                }
            }
            destroy { appModule ->
                val tab: Tab? = moduleTabs.remove(appModule)
                tabModules.remove(tab)
            }
        }
    }
}
