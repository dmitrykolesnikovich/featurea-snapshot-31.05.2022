package featurea.studio.project.components

import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.import
import featurea.studio.project.Project
import javafx.event.EventHandler

fun Component.installProjectMenuAction(menuTitle: String, menuItemTitle: String, action: () -> Unit) {
    val project: Project = import()
    runOnJfxThread {
        project.menuBar.findMenuItem(menuTitle, menuItemTitle).apply {
            onAction = EventHandler { action() }
        }
    }
}
