package featurea.math

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

const val PI: Float = 3.14159f

fun cosDeg(degree: Double): Double {
    val radians = degree.toRadians()
    return cos(radians)
}

fun sinDeg(degree: Double): Double {
    val radians = degree.toRadians()
    return sin(radians)
}

fun nextPowerOfTwo(value: Int): Int {
    var value = value
    if (value == 0) {
        return 1
    }
    value--
    value = value or (value shr 1)
    value = value or (value shr 2)
    value = value or (value shr 4)
    value = value or (value shr 8)
    value = value or (value shr 16)
    return value + 1
}

fun minIndex(vararg values: Float): Int {
    if (values.isEmpty()) return -1
    var minValue = values[0]
    var minIndex = 0
    for (index in values.indices) {
        if (minValue > values[index]) {
            minValue = values[index]
            minIndex = index
        }
    }
    return minIndex
}

fun min(a: Float, b: Float): Float = if (a <= b) a else b
fun max(a: Float, b: Float): Float = if (a >= b) a else b
fun min(a: Double, b: Double): Double = if (a <= b) a else b
fun max(a: Double, b: Double): Double = if (a >= b) a else b
fun cbrt(value: Int): Double = value.toDouble().pow(1 / 3.toDouble())
fun cbrt(value: Double): Double = value.pow(1 / 3.toDouble())
fun Double.toRadians(): Double = this / 180.0 * PI
fun Double.toDegrees(): Double = this / PI * 180.0
fun Float.toRadians(): Float = this / 180f * featurea.math.PI
fun Float.toDegrees(): Float = this / featurea.math.PI * 180f

fun clamp(value: Double, min: Double = 0.0, max: Double = 1.0): Double {
    return if (value < min) min else if (value > max) max else value
}

fun clamp(value: Float, min: Float = 0f, max: Float = 1f): Float {
    return if (value < min) min else if (value > max) max else value
}

fun clamp(value: Int, min: Int = 0, max: Int = 1): Int {
    return if (value < min) min else if (value > max) max else value
}
