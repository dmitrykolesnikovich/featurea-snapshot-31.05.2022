package featurea.opengl

import featurea.utils.Color
import featurea.utils.FloatBuffer

abstract class Buffer(val drawCallSize: Int, val isMedium: Boolean) {

    var data: FloatBuffer = FloatBuffer(limit = 0, isMedium)
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
            data = FloatBuffer(drawCallLimit * drawCallSize, isMedium)
        }
    }

}

fun Buffer.vertex(vararg floats: Float, color: Color) {
    val (r, g, b, a) = color
    vertex(*floats, r, g, b, a)
}
