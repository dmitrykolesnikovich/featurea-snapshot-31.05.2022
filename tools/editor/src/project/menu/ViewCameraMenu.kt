package featurea.studio.project.menu

import featurea.Application
import featurea.desktop.runOnJfxThread
import featurea.studio.runOnUpdateOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import featurea.window.Window
import javafx.event.EventHandler

class ViewCameraMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findCheckMenuItem("View", "Camera").apply {
                onAction = EventHandler {
                    val selectedEditor = project.findSelectedEditorOrNull() ?: return@EventHandler

                    val app: Application = selectedEditor.import()
                    val window: Window = selectedEditor.import()

                    window.useCamera = isSelected
                    app.runOnUpdateOnJfxThread {
                        window.invalidate()
                    }
                }
            }
        }
    }

}
