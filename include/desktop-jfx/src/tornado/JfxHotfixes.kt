package featurea.desktop.jfx

import featurea.jvm.field
import featurea.jvm.staticValue
import javafx.application.Platform
import javafx.stage.Stage
import kotlin.system.exitProcess

fun Stage.initDesktopTheme() = Platform.runLater {
    setOnCloseRequest {
        Platform.exit()
        exitProcess(0)
    }
    applyTreeViewHotfixes()
}

@Suppress("NewApi")
private fun applyTreeViewHotfixes() {
    val bindingsField = field("com.sun.javafx.scene.control.behavior.TreeViewBehavior", "TREE_VIEW_BINDINGS")
    val bindings: MutableList<*> = bindingsField.staticValue()
    // bindings is MutableList<com.sun.javafx.scene.control.behavior.KeyBinding>
    bindings.removeIf {
        it.field("action").get(it) == "SelectAll"
    }
}
