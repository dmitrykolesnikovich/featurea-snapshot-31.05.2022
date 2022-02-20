package featurea.input

import featurea.layout.Camera
import featurea.math.Vector2
import featurea.window.Window
import featurea.window.toLocalCoordinates

data class InputEvent(
    val source: InputEventSource,
    val type: InputEventType,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val scrollX: Float = 0f, // quickfix todo avoid default value
    val scrollY: Float = 0f, // quickfix todo avoid default value
    val preventDefaultBlock: (() -> Unit)? = null // works on js target only
)

enum class InputEventSource {
    LEFT,         // works on all targets
    RIGHT, MIDDLE // works on desktop targets only
}

enum class InputEventType {
    DOWN, DRAG, UP, DOUBLE_CLICK, // works on all targets
    LONG_TOUCH,                   // works on mobile targets only
    MOVE,                         // works on desktop targets only
    WHEEL,                        // works on desktop targets only
}

fun InputEvent.preventDefault() {
    preventDefaultBlock?.invoke()
}

val InputEvent.isDoubleClick: Boolean get() = type == InputEventType.DOUBLE_CLICK
val InputEvent.isDown: Boolean get() = type == InputEventType.DOWN
val InputEvent.isDrag: Boolean get() = type == InputEventType.DRAG
val InputEvent.isLongTouch: Boolean get() = type == InputEventType.LONG_TOUCH
val InputEvent.isMove: Boolean get() = type == InputEventType.MOVE
val InputEvent.isUp: Boolean get() = type == InputEventType.UP

fun Window.toLocalEvent(camera: Camera, event: InputEvent, vr: Vector2.Result = Vector2().Result()): InputEvent {
    val (x1, y1) = toLocalCoordinates(camera, event.x1, event.y1, vr)
    val (x2, y2) = toLocalCoordinates(camera, event.x2, event.y2, vr)
    val localEvent: InputEvent = InputEvent(event.source, event.type, x1, y1, x2, y2, event.scrollX, event.scrollY)
    return localEvent
}

fun InputEvent.withType(type: InputEventType): InputEvent =
    InputEvent(source, type, x1, y1, x2, y2, scrollX, scrollY, preventDefaultBlock)
