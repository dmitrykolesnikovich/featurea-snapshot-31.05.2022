package featurea.studio.editor.features

import featurea.desktop.MainPanelProxy
import featurea.desktop.jfx.onMouseEvent
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

class MouseService(override val module: Module) : Component {

    private val mainPanel: JPanel = import(MainPanelProxy)

    var isInsideEditor: Boolean = false
        private set
    var isInsideEditorWithTouch: Boolean = false
        private set
    private val mouseMoveListeners = mutableListOf<MouseAdapter>()

    init {
        mainPanel.onMouseEvent(object : MouseAdapter() {

            override fun mousePressed(event: MouseEvent) {
                isInsideEditor = true
                isInsideEditorWithTouch = true
            }

            override fun mouseDragged(event: MouseEvent) {
                isInsideEditor = true
                isInsideEditorWithTouch = true
            }

            override fun mouseReleased(event: MouseEvent) {
                isInsideEditor = true
                isInsideEditorWithTouch = false
            }

            override fun mouseMoved(event: MouseEvent) {
                isInsideEditor = true
                isInsideEditorWithTouch = false
                for (mouseMoveListener in mouseMoveListeners) mouseMoveListener.mouseMoved(event)
            }

            override fun mouseEntered(event: MouseEvent) {
                isInsideEditor = true
                isInsideEditorWithTouch = false
            }

            override fun mouseExited(event: MouseEvent) {
                isInsideEditor = false
                isInsideEditorWithTouch = false
            }

        })
    }

    fun onMove(block: (event: MouseEvent) -> Unit) {
        mouseMoveListeners.add(object : MouseAdapter() {
            override fun mouseMoved(event: MouseEvent) = block(event)
        })
    }

}
