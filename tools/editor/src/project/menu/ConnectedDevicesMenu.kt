package featurea.studio.project.menu

import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import featurea.studio.project.components.openExternalEditor
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class ConnectedDevicesMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Window", "Connected Devices").apply {
                onAction = EventHandler {
                    project.openExternalEditor<Any>("featurea.deviceChooser.studio.DevicesChooserDialog.show()")
                }
                accelerator = KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }

}
