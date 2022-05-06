package featurea.opengl

import featurea.utils.Color
import featurea.FloatBuffer
import featurea.math.Point
import featurea.math.Precision

abstract class Buffer(val stride: Int, val attributesPerDraw: Int, val checkMediumPrecision: Boolean) {

    var data: FloatBuffer = FloatBuffer(limit = 0, checkMediumPrecision)
        private set
    var drawCallLimit: Int = 0
        private set
    var vertexCount: Int = 0
        private set
    var isDirty: Boolean = true
        internal set
    val isNotDirty: Boolean
        get() = !isDirty
    val isEmpty: Boolean get() = data.size == 0
    val isNotEmpty: Boolean get() = data.size != 0

    fun vertex(vararg attributes: Float) {
        data.pushAll(attributes)
        vertexCount++
    }

    fun clear(drawCallLimit: Int) {
        clear()
        ensureDrawCallLimit(drawCallLimit)
    }

    fun clear() {
        isDirty = true
        data.clear()
        vertexCount = 0
    }

    fun increaseDrawCallLimit(drawCallLimit: Int) {
        if (this.drawCallLimit < drawCallLimit) {
            ensureDrawCallLimit(drawCallLimit)
        }
    }

    fun ensureDrawCallLimit(drawCallLimit: Int) {
        if (this.drawCallLimit != drawCallLimit) {
            clear()
            this.drawCallLimit = drawCallLimit
            data = FloatBuffer(drawCallLimit * attributesPerDraw, checkMediumPrecision)
        }
    }

}

fun Buffer.vertex(vararg floats: Float, color: Color) {
    val (r, g, b, a) = color
    vertex(*floats, r, g, b, a)
}
