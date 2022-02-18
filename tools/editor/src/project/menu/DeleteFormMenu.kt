package featurea.studio.project.menu

import featurea.config.Config
import featurea.desktop.runOnJfxThread
import featurea.runtime.Module
import featurea.runtime.Component
import featurea.runtime.import
import featurea.studio.project.Project
import javafx.event.EventHandler

class DeleteFormMenu(override val module: Module) : Component {

    private val project: Project = import()
    private val uiConfig = Config("ui")

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("File", uiConfig["deleteDocument", "Delete Document"]).apply {
                onAction = EventHandler {
                    val editor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    project.removeDocument(editor.tab.id)
                }
            }
        }
    }

}
