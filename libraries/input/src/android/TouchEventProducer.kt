package featurea.input

import android.annotation.TargetApi
import android.os.Build
import android.view.MotionEvent
import android.view.View
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class TouchEventProducer constructor(override val module: Module) : Component, View.OnLongClickListener {

    private val input: Input = import()

    private var x1 = 0f
    private var y1 = 0f
    private var x2 = 0f
    private var y2 = 0f
    private var pointerIdLongPress = 0
    private var hasTouch = false

    override fun onLongClick(view: View?): Boolean {
        if (hasTouch) {
            input.addEvent(InputEvent(InputEventSource.LEFT, InputEventType.LONG_TOUCH, x1, y1, x2, y2))
        }
        return false
    }

    fun onTouchEvent(event: MotionEvent) {
        run {
            val pointerIndex = event.actionIndex
            val pointerId = event.getPointerId(pointerIndex)
            val x = event.getX(pointerIndex)
            val y = event.getY(pointerIndex)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    x1 = x
                    y1 = y
                    x2 = x1
                    y2 = y1
                    pointerIdLongPress = pointerId
                    hasTouch = true
                    input.addEvent(InputEvent(InputEventSource.LEFT, InputEventType.DOWN, x, y, x, y))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    input.addEvent(InputEvent(InputEventSource.LEFT, InputEventType.UP, x, y, x, y))
                    clear()
                }
            }
        }
        run {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    var pointerIndex = 0
                    while (pointerIndex < event.pointerCount) {
                        val x = event.getX(pointerIndex)
                        val y = event.getY(pointerIndex)
                        x2 = x
                        y2 = y
                        val pointerId = event.getPointerId(pointerIndex)
                        input.addEvent(InputEvent(InputEventSource.LEFT, InputEventType.DRAG, x, y, x, y))
                        pointerIndex++
                    }
                }
            }
        }
    }

    private fun clear() {
        x1 = -1f
        y1 = -1f
        x2 = -1f
        y2 = -1f
        pointerIdLongPress = -1
        hasTouch = false
    }

}
