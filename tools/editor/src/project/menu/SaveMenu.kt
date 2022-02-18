package featurea.studio.project.menu

import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN

class SaveMenu(override val module: Module) : Component {

    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("File", "Save").apply {
                onAction = EventHandler {
                    val editor = project.findSelectedEditorOrNull() ?: return@EventHandler
                    editor.tab.save()
                }
                accelerator = KeyCodeCombination(KeyCode.S, SHORTCUT_DOWN)
            }
        }
    }

}
