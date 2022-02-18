package featurea.input

import featurea.input.InputEventSource.LEFT
import featurea.input.InputEventSource.MIDDLE
import featurea.input.InputEventType.*
import featurea.js.HtmlElementProxy
import featurea.math.Point
import featurea.math.Vector2
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import kotlinx.browser.window as jsWindow
import org.w3c.dom.events.EventListener as JsEventListener

class MouseEventProducer(override val module: Module) : Component, JsEventListener {

    private val htmlElement: HTMLElement = import(HtmlElementProxy)
    private val input: Input = import()

    private var lastDragX: Float? = null
    private var lastDragY: Float? = null
    private var isDown = false

    override fun onCreateComponent() {
        htmlElement.addEventListener("mouseup", this, false)
        htmlElement.addEventListener("mousedown", this, false)
        htmlElement.addEventListener("mousemove", this, false)
        htmlElement.addEventListener("dblclick", this, false)
        htmlElement.addEventListener("wheel", this, false)
    }

    override fun onDeleteComponent() {
        htmlElement.removeEventListener("mousedown", this)
        htmlElement.removeEventListener("mouseup", this)
        htmlElement.removeEventListener("mousemove", this)
        htmlElement.removeEventListener("dblclick", this)
        htmlElement.removeEventListener("wheel", this)
    }

    override fun handleEvent(event: Event) {
        event as MouseEvent
        when (event.type) {
            "mousedown" -> {
                reset()
                val (x, y) = event.findMousePosition()
                isDown = true
                input.addEvent(InputEvent(LEFT, DOWN, x, y, x2 = x, y2 = y))
            }
            "mouseup" -> {
                reset()
                val (x, y) = event.findMousePosition()
                isDown = false
                input.addEvent(InputEvent(LEFT, UP, x, y, x2 = x, y2 = y))
            }
            "mousemove" -> {
                if (isDown) {
                    val (x, y) = event.findMousePosition()
                    input.addEvent(InputEvent(LEFT, DRAG, lastDragX ?: x, lastDragY ?: y, x2 = x, y2 = y))
                    lastDragX = x
                    lastDragY = y
                } else {
                    reset()
                    val (x, y) = event.findMousePosition()
                    input.addEvent(InputEvent(LEFT, MOVE, x, y, x2 = x, y2 = y))
                }
            }
            "dblclick" -> {
                reset()
                val (x, y) = event.findMousePosition()
                input.addEvent(InputEvent(LEFT, DOUBLE_CLICK, x, y, x2 = x, y2 = y))
            }
            "wheel" -> {
                event as WheelEvent
                reset()
                val (x, y) = event.findMousePosition()
                val scrollX: Float = event.deltaX.toFloat()
                val scrollY: Float = event.deltaY.toFloat()
                input.addEvent(InputEvent(MIDDLE, WHEEL, x, y, x2 = x, y2 = y, scrollX, scrollY, event::preventDefault))
            }
        }
    }

    /*internals*/

    private fun reset() {
        lastDragX = null
        lastDragY = null
    }

    // https://www.khronos.org/webgl/wiki/HandlingHighDPI
    private fun MouseEvent.findMousePosition(): Vector2.Result {
        val rectangle = htmlElement.getBoundingClientRect()
        val x = (clientX - rectangle.left) * jsWindow.devicePixelRatio
        val y = (clientY - rectangle.top) * jsWindow.devicePixelRatio
        return Point().Result().apply(x.toFloat(), y.toFloat())
    }

}
