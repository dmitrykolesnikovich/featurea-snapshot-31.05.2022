package featurea.studio.project.menu

import featurea.app.Application
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import featurea.window.Window
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

class ViewSimulatorMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findCheckMenuItem("View", "Simulator").apply {
                accelerator = KeyCodeCombination(KeyCode.F4)
                onAction = EventHandler {
                    val selectedEditor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    val app = selectedEditor.import<Application>()
                    val window = selectedEditor.import<Window>()

                    app.isEnable = isSelected
                    window.invalidate()
                }
            }
        }
    }

}
