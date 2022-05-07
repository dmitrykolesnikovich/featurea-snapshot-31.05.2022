package featurea.modbus.support;

import featurea.utils.Specified

enum class DataType(override val specifier: String, val size: Short, val normalize: (Float) -> Float) : Specified {
    Int16("Int16", size = 1, normalize = { Int16(it).normalizedValue }),
    UInt16("UInt16", size = 1, normalize = { UInt16(it).normalizedValue }),
    Int32("Int32", size = 2, normalize = { Int32(it).normalizedValue }),
    UInt32("UInt32", size = 2, normalize = { UInt32(it).normalizedValue }),
    Float32("Float32", size = 2, normalize = { Float32(it).normalizedValue });
}

class Int16(val initialValue: Float) {

    val normalizedValue: Float = normalize(initialValue, MIN_VALUE, MAX_VALUE)

    companion object {
        const val MIN_VALUE: Float = -32768f
        const val MAX_VALUE: Float = 32767f
    }

}

class UInt16(val initialValue: Float) {

    val normalizedValue: Float = normalize(initialValue, MIN_VALUE, MAX_VALUE)

    companion object {
        const val MIN_VALUE: Float = 0f
        const val MAX_VALUE: Float = 65_535f
    }

}

class Int32(val initialValue: Float) {

    val normalizedValue: Float = normalize(initialValue, MIN_VALUE, MAX_VALUE)

    companion object {
        const val MIN_VALUE: Float = -2147483648f
        const val MAX_VALUE: Float = 2147483647f
    }

}

class UInt32(val initialValue: Float) {

    val normalizedValue: Float = normalize(initialValue, MIN_VALUE, MAX_VALUE)

    companion object {
        const val MIN_VALUE: Float = 0f
        const val MAX_VALUE: Float = 4_294_967_295f
    }

}

class Float32(val initialValue: Float) {

    val normalizedValue: Float = initialValue

    companion object {
        const val MAX_VALUE: Float = 3.4028235e+38f
        const val MIN_VALUE: Float = -MAX_VALUE
    }

}

/*internals*/

private fun normalize(floatValue: Float, floatMin: Float, floatMax: Float): Float {
    val value = floatValue.toDouble()
    val min = floatMin.toDouble()
    val max = floatMax.toDouble()
    val step = max + 1 - min
    val delta = value - min
    val normalizedDelta = delta % step
    var result = min + normalizedDelta
    if (normalizedDelta < 0) result += max + 1
    val floatResult = result.toFloat()
    return floatResult
}
