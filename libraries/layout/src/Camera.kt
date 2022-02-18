package featurea.layout

import featurea.math.Coordinates
import featurea.math.Rectangle
import featurea.math.Size
import featurea.math.Surface
import featurea.splitAndTrim

class Camera {

    val style: CameraStyle = CameraStyle(this)
    val surface: Surface = Surface()

    constructor(init: Camera.() -> Unit = {}) {
        init()
    }

    constructor(original: Camera) {
        assign(original)
    }

    fun assign(original: Camera) {
        surface.transform.edit { assign(original.surface.transform) }
        surface.viewport.assign(original.surface.viewport)
        surface.origin.assign(original.surface.origin)
        surface.size.assign(original.surface.size)
    }

    fun assign(value: String) {
        val (x, y, width, height) = value.splitAndTrim(",").map { it.toFloat() }
        surface.origin.assign(x, y)
        surface.size.assign(width, height)
    }

    fun resize(width: Int, height: Int) {
        style.layoutCamera(Size(width, height)) // quickfix todo improve
    }

    fun resize(size: Size) {
        style.layoutCamera(size)
    }

}

/*convenience*/

val Camera.coordinates: Coordinates get() = surface.coordinates
val Camera.x: Float get() = surface.origin.x
val Camera.y: Float get() = surface.origin.y

fun Camera.toScissorRectangle(): Rectangle.Result {
    val x1 = surface.transform.tx
    val y1 = surface.transform.ty
    val x2 = x1 + surface.viewport.width
    val y2 = y1 + surface.viewport.height
    return rectangleResult.assign(x1, y1, x2, y2)
}

private val rectangleResult: Rectangle.Result = Rectangle().Result()
