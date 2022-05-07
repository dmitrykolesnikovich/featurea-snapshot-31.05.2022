package featurea.layout

import featurea.math.*
import featurea.utils.splitAndTrim

class Camera {

    val style: CameraStyle = CameraStyle(this)
    val surface: Surface = Surface()
    private val sizeParameter = Size()

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

    fun resize(width: Float, height: Float) {
        resize(sizeParameter.assign(width, height))
    }

    fun resize(size: Size) {
        fun center(isVertical: Boolean, size: Size) {
            val tx: Float
            val ty: Float
            if (isVertical) {
                tx = 0f
                val newCameraHeight = surface.size.height * surface.transform.sy
                ty = (size.height - newCameraHeight) / 2
                surface.viewport.width = size.width
                surface.viewport.height = newCameraHeight
            } else {
                val newCameraWidth = surface.size.width * surface.transform.sx
                tx = (size.width - newCameraWidth) / 2
                ty = 0f
                surface.viewport.width = newCameraWidth
                surface.viewport.height = size.height
            }
            surface.transform.edit { assignTranslation(tx, ty) } // quickfix todo conceptualize
        }

        fun anchor(isVertical: Boolean, size: Size) {
            if (isVertical) {
                style.vertical?.layout?.invoke(this, size)
            } else {
                style.horizontal?.layout?.invoke(this, size)
            }
        }

        surface.transform.edit { assignOrigin(surface.origin) } // quickfix todo conceptualize
        if (size.isNotEmpty() && surface.size.isNotEmpty()) {
            val widthRatio = size.width / surface.size.width
            val heightRatio = size.height / surface.size.height
            val minRatio = min(widthRatio, heightRatio)
            surface.transform.edit { assignScale(minRatio, minRatio) } // quickfix todo conceptualize
            val isVertical = widthRatio == minRatio
            center(isVertical, size)
            anchor(isVertical, size)
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
