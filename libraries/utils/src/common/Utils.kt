package featurea.utils

import kotlin.text.toFloat as convertStringToFloat
import kotlin.text.toInt as convertStringToInt

fun <T : Number> checkNotZero(value: T): T {
    check(value != 0)
    return value
}

@ExperimentalUnsignedTypes
fun checkNotZero(value: UInt): UInt {
    check(value != 0u)
    return value
}

fun String?.toFloat(default: Float = 0f): Float {
    if (this == null || isBlank()) return default
    try {
        return convertStringToFloat()
    } catch (e: Exception) {
        return default
    }
}

fun String?.toInt(default: Int = 0): Int {
    if (this == null || isBlank()) return default
    try {
        return convertStringToInt()
    } catch (e: Exception) {
        return default
    }
}

fun List<Any?>.firstStringOrNull(): String? {
    val value: Any? = firstOrNull()
    return if (value is String) value else null
}

fun List<Any?>.firstString(): String {
    return firstStringOrNull() ?: error("list: ${joinToString()}")
}
