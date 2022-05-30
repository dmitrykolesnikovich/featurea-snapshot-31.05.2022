package featurea.studio.editor.features

import featurea.desktop.MainNodeProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.Editor
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class ClearValueEditorFeature(override val module: Module) : Component {

    private val editor: Editor = import()
    private val mainNode: Node = import(MainNodeProxy)

    init {
        mainNode.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            if (event.code == KeyCode.F3) {
                editor.selectionService.clearSelectedValue()
                event.consume()
            }
        }
    }

}
