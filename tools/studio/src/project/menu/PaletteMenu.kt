package featurea.studio.project.menu

import featurea.config.Config
import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import featurea.studio.project.components.Palette
import javafx.event.EventHandler

class PaletteMenu(override val module: Module) : Component {

    private val palette: Palette = import()
    private val project: Project = import()
    private val uiConfig: Config = Config("ui")

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Window", uiConfig["paletteMenuItem", "Palette"]).apply {
                onAction = EventHandler {
                    palette.show()
                }
            }
        }
    }

}
