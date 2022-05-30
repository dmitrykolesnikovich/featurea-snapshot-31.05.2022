package featurea.studio.project.menu

import featurea.runtime.Component
import featurea.runtime.executeAction
import featurea.runtime.import
import featurea.studio.project.Project
import javafx.event.EventHandler

fun Component.openProjectMenu() {
    val project: Project = import()
    project.menuBar.findMenuItem("File", "Open Project...").onAction = EventHandler {
        executeAction("featurea.studio.openProject")
    }
}
