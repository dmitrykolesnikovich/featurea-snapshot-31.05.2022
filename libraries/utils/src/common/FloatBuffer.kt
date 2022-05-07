package featurea.utils

private const val MEDIUM_PRECISION_FLOAT_MAX: Float = 65504f // quickfix todo find better place

// used by VertexBuffer: pushAll -> toFloatArray -> clear
class FloatBuffer(val limit: Int, private val isMedium: Boolean) {

    private val array: FloatArray = FloatArray(limit)
    var size: Int = 0
        private set

    fun pushAll(values: FloatArray) {
        for (value in values) {
            push(value)
        }
    }

    fun push(value: Float) {
        if (size >= limit) {
            error("limit exceeded: $limit")
        }
        if (isMedium || alwaysCheckMediumPrecision) {
            if (value > MEDIUM_PRECISION_FLOAT_MAX) {
                error("precision exceeded: $value")
            }
        }
        array[size] = value
        size++
    }

    fun toFloatArray(): FloatArray {
        return array
    }

    fun clear() {
        size = 0
    }

}
