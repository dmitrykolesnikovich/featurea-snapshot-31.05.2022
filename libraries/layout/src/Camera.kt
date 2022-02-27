package featurea.layout

import featurea.math.*
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

    constructor(size: Size) {
        surface.size.assign(size)
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

    fun resize(size: Size) {
        resize(size.width, size.height)
    }

    // todo make use of `style`
    fun resize(width: Float, height: Float) {
        // setup
        surface.transform.edit { assignOrigin(surface.origin) } // quickfix todo make automatically consistent

        // filter
        if (width == 0f || height == 0f) return
        if (size.width == 0f || size.height == 0f) return

        // action
        val wratio: Float = width / size.width
        val hratio: Float = height / size.height
        if (wratio < hratio) {
            surface.viewport.assign(size.width, size.height * wratio)
            surface.transform.edit {
                assignScale(wratio)
                assignTranslation(tx = 0f, ty = (height - surface.viewport.height) / 2f)
            }
        } else {
            surface.viewport.assign(size.width * hratio, size.height)
            surface.transform.edit {
                assignScale(hratio)
                assignTranslation(tx = (width - surface.viewport.width) / 2f, ty = 0f)
            }
        }
    }

}

/*convenience*/

val Camera.coordinates: Coordinates get() = surface.coordinates
val Camera.x: Float get() = surface.origin.x
val Camera.y: Float get() = surface.origin.y
val Camera.size: Size get() = surface.size

fun Camera.toScissorRectangle(): Rectangle.Result {
    val x1: Float = surface.transform.tx
    val y1: Float = surface.transform.ty
    val x2: Float = x1 + surface.viewport.width
    val y2: Float = y1 + surface.viewport.height
    return rectangleResult.assign(x1, y1, x2, y2)
}

private val rectangleResult: Rectangle.Result = Rectangle().Result()
