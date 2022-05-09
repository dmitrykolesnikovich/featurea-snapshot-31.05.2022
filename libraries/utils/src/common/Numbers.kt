@file:Suppress("EXPERIMENTAL_API_USAGE")

package featurea.utils

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

const val FLOAT_ROUNDING_ERROR: Float = 0.000001f // 32 bits

fun <T : Number> checkNotZero(value: T): T {
    check(value != 0)
    return value
}

fun checkNotZero(value: UInt): UInt {
    check(value != 0u)
    return value
}

fun Double.calibrateByFractionSize(fractionSize: Int): Double {
    check(fractionSize >= 0)
    if (fractionSize == 0) return round(this)
    val base: Double = 10.0.pow(-fractionSize)
    return calibrateByBase(base)
}

fun Double.calibrateByBase(base: Double): Double {
    check(base > 0)
    return round(this / base) * base
}

fun Double.calibrateByFractionSizeToString(fractionSize: Int): String {
    val base: Double = 10.0.pow(-fractionSize)
    return calibrateByBaseToString(base)
}

val Double.fractionSize: Int
    get() {
        val tokens: List<String> = toDoubleString().split(".")
        return when (tokens.size) {
            1 -> 0
            2 -> tokens[1].length
            else -> error("tokens.size: ${tokens.size}")
        }
    }

fun Double.calibrateByBaseToString(base: Double): String {
    if (base == 0.0) return toDoubleString()
    val calibratedValue: Double = calibrateByBase(base)
    var doubleString: String = calibratedValue.toDoubleString()
    val fractionSize: Int = base.fractionSize
    val result = if (fractionSize == 0) {
        val indexOfDot = doubleString.indexOf('.')
        if (indexOfDot != -1) doubleString.substring(0, indexOfDot) else doubleString
    } else {
        if (!doubleString.contains(".")) doubleString += "."            // 25.40000000002
        val indexOfDot: Int = doubleString.indexOf('.')                 // 2
        val fraction: String = doubleString.substring(indexOfDot + 1)   // 40000000002
        val integer: String = doubleString.substring(0, indexOfDot)     // 40000000002
        if (fraction.length < fractionSize) {                           // 11 < 2
            val countOfZeros: Int = fractionSize - fraction.length
            repeat(countOfZeros) {
                doubleString += "0"
            }
        } else if (fraction.length > fractionSize) {
            val newFraction: String = fraction.substring(0, fractionSize)
            doubleString = "${integer}.${newFraction}"
        }
        doubleString
    }
    return result
}

expect fun Double.toDoubleString(): String

fun Float.toHexString(base: Int): String {
    val hexString: String = (this * base).toInt().toString(16).toUpperCase()
    return if (hexString.length == 1) "0${hexString}" else hexString
}

fun Float.isEqualFloat(b: Float): Boolean {
    return abs(this - b) <= FLOAT_ROUNDING_ERROR
}

fun Float.isZeroFloat(): Boolean {
    return abs(this) <= FLOAT_ROUNDING_ERROR
}
