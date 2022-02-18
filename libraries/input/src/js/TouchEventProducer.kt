package featurea.input

import featurea.getTimeMillis
import featurea.input.InputEventSource.LEFT
import featurea.input.InputEventType.*
import featurea.js.HTMLCanvasElementProxy
import featurea.js.HtmlElementProxy
import featurea.js.applyCssAttribute
import featurea.math.Point
import featurea.math.Vector2
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlinx.browser.window as jsWindow
import org.w3c.dom.events.EventListener as JsEventListener

// https://developer.mozilla.org/en-US/docs/Web/API/Touch_events
class TouchEventProducer(override val module: Module) : Component, JsEventListener {

    private val htmlElement: HTMLElement = import(HtmlElementProxy)
    private val input: Input = import()
    private val mainCanvas: HTMLCanvasElement = import(HTMLCanvasElementProxy)

    private var lastDragX: Float? = null
    private var lastDragY: Float? = null
    private var isDown: Boolean = false
    private var isDoubleClick: Boolean = false
    private var firstClickTime: Double = -1.0;

    init {
        mainCanvas.style.applyCssAttribute("touch-action" to "none")
    }

    override fun onCreateComponent() {
        htmlElement.addEventListener("touchstart", this, false)
        htmlElement.addEventListener("touchend", this, false)
        htmlElement.addEventListener("touchcancel", this, false)
        htmlElement.addEventListener("touchmove", this, false)
    }

    override fun onDeleteComponent() {
        htmlElement.removeEventListener("touchstart", this)
        htmlElement.removeEventListener("touchend", this)
        htmlElement.removeEventListener("touchcancel", this)
        htmlElement.removeEventListener("touchmove", this)
    }

    override fun handleEvent(event: Event) {
        event as TouchEvent
        when (event.type) {
            "touchstart" -> {
                val now = getTimeMillis()
                isDoubleClick = now - firstClickTime < DEFAULT_DOUBLE_CLICK_DELAY
                firstClickTime = if (isDoubleClick) -1.0 else now

                reset()
                val (x, y) = event.findTouchPosition()
                isDown = true
                input.addEvent(InputEvent(LEFT, DOWN, x, y, x2 = x, y2 = y))

                // >> quickfix for `libraries/keyboard/src/js/KeyboardDelegate.kt` todo improve on Safari
                GlobalScope.launch {
                    input.update()
                }
                // <<
            }
            "touchcancel", "touchend" -> {
                reset()
                val (x, y) = event.findTouchPosition()
                isDown = false
                input.addEvent(InputEvent(LEFT, UP, x, y, x2 = x, y2 = y))
                if (isDoubleClick) {
                    input.addEvent(InputEvent(LEFT, DOUBLE_CLICK, x, y, x2 = x, y2 = y))
                }
            }
            "touchmove" -> {
                if (isDown) {
                    val (x, y) = event.findTouchPosition()
                    input.addEvent(InputEvent(LEFT, DRAG, lastDragX ?: x, lastDragY ?: y, x2 = x, y2 = y, preventDefaultBlock = event::preventDefault))
                    lastDragX = x
                    lastDragY = y
                }
            }
        }
    }

    /*internals*/

    private fun reset() {
        lastDragX = null
        lastDragY = null
    }

    // https://www.khronos.org/webgl/wiki/HandlingHighDPI
    private fun TouchEvent.findTouchPosition(): Vector2.Result {
        val rectangle: DOMRect = htmlElement.getBoundingClientRect()
        val touch: Touch = changedTouches[0] ?: error("changedTouches: $changedTouches")
        val x: Double = (touch.clientX - rectangle.left) * jsWindow.devicePixelRatio
        val y: Double = (touch.clientY - rectangle.top) * jsWindow.devicePixelRatio
        return Point().Result().apply(x.toFloat(), y.toFloat())
    }

}
