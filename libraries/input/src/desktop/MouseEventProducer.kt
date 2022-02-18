package featurea.input

import featurea.desktop.MainPanelProxy
import featurea.input.InputEventSource.LEFT
import featurea.input.InputEventSource.MIDDLE
import featurea.input.InputEventType.*
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.JPanel

class MouseEventProducer constructor(override val module: Module) : Component, MouseAdapter() {

    private val input: Input = import()
    private val mainPanel: JPanel = import(MainPanelProxy)

    private var lastDragX: Float? = null
    private var lastDragY: Float? = null

    override fun onCreateComponent() {
        mainPanel.addMouseListener(this)
        mainPanel.addMouseMotionListener(this)
        mainPanel.addMouseWheelListener(this)
    }

    override fun onDeleteComponent() {
        mainPanel.removeMouseListener(this)
        mainPanel.removeMouseMotionListener(this)
        mainPanel.removeMouseWheelListener(this)
    }

    override fun mousePressed(mouseEvent: MouseEvent?) {
        if (mouseEvent == null) return
        val x = mouseEvent.x.toFloat()
        val y = mouseEvent.y.toFloat()
        input.addEvent(InputEvent(source = LEFT, type = DOWN, x1 = x, y1 = y, x2 = x, y2 = y))
    }

    override fun mouseReleased(mouseEvent: MouseEvent?) {
        reset()
        if (mouseEvent == null) return
        val x = mouseEvent.x.toFloat()
        val y = mouseEvent.y.toFloat()
        input.addEvent(InputEvent(source = LEFT, type = UP, x1 = x, y1 = y, x2 = x, y2 = y))
    }

    override fun mouseDragged(mouseEvent: MouseEvent?) {
        if (mouseEvent == null) return
        val x = mouseEvent.x.toFloat()
        val y = mouseEvent.y.toFloat()
        input.addEvent(InputEvent(source = LEFT, type = DRAG, x1 = lastDragX ?: x, y1 = lastDragY ?: y, x2 = x, y2 = y))
        lastDragX = x
        lastDragY = y
    }

    override fun mouseMoved(mouseEvent: MouseEvent?) {
        reset()
        if (mouseEvent == null) return
        val x = mouseEvent.x.toFloat()
        val y = mouseEvent.y.toFloat()
        input.addEvent(InputEvent(source = LEFT, type = MOVE, x1 = x, y1 = y, x2 = x, y2 = y))
    }

    override fun mouseClicked(mouseEvent: MouseEvent?) {
        reset()
        if (mouseEvent == null) return
        if (mouseEvent.clickCount != 2) return
        val x = mouseEvent.x.toFloat()
        val y = mouseEvent.y.toFloat()
        input.addEvent(InputEvent(source = LEFT, type = DOUBLE_CLICK, x1 = x, y1 = y, x2 = x, y2 = y))
    }

    override fun mouseWheelMoved(event: MouseWheelEvent?) {
        reset()
        if (event == null) return
        val x = event.x.toFloat()
        val y = event.y.toFloat()
        val scrollAmount: Float = event.wheelRotation.toFloat()
        input.addEvent(InputEvent(source = MIDDLE, type = WHEEL, x1 = x, y1 = y, x2 = x, y2 = y, scrollY = scrollAmount))
    }

    override fun mouseExited(mouseEvent: MouseEvent?) {
        reset()
    }

    private fun reset() {
        lastDragX = null
        lastDragY = null
    }

}
