package featurea.studio.project.menu

import featurea.desktop.runOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.project.Project
import featurea.studio.project.components.Clipboard
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class CutCopyPasteMenu(override val module: Module) : Component {

    private val clipboard: Clipboard = import()
    private val project: Project = import()

    init {
        runOnJfxThread {
            project.menuBar.findMenuItem("Edit", "Cut").apply {
                onAction = EventHandler { clipboard.cut() }
                accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Copy").apply {
                onAction = EventHandler { clipboard.copy() }
                accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN)
            }
            project.menuBar.findMenuItem("Edit", "Paste").apply {
                onAction = EventHandler {
                    println("paste")
                    clipboard.paste()
                }
                accelerator = KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }

}
