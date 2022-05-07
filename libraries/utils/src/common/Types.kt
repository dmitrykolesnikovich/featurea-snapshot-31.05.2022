package featurea

import featurea.utils.toUpperCaseFirst
import kotlin.reflect.KClass

private val decimalRegex = "^([0-9]*)\\.([0-9]*)$".toRegex()
private val integerRegex = "^[0-9]+\$".toRegex()
private val booleanRegex = "false|true".toRegex()

val primitiveClasses = mapOf<String, KClass<*>>(
    "boolean" to Boolean::class,
    "boolean[]" to BooleanArray::class,
    "byte" to Byte::class,
    "byte[]" to ByteArray::class,
    "char" to Char::class,
    "char[]" to CharArray::class,
    "short" to Short::class,
    "short[]" to ShortArray::class,
    "int" to Int::class,
    "int[]" to IntArray::class,
    "long" to Long::class,
    "long[]" to LongArray::class,
    "float" to Float::class,
    "float[]" to FloatArray::class,
    "double" to Double::class,
    "double[]" to DoubleArray::class
)

val numberClasses = listOf(
    "byte", "byte[]",
    "short", "short[]",
    "int", "int[]",
    "long", "long[]",
    "float", "float[]",
    "double", "double[]"
)

val defaultValueByPrimitiveType = mapOf(
    "boolean" to "false",
    "boolean[]" to "null",
    "byte" to "0",
    "byte[]" to "null",
    "char" to "0",
    "char[]" to "null",
    "short" to "0",
    "short[]" to "null",
    "int" to "0",
    "int[]" to "null",
    "long" to "0",
    "long[]" to "null",
    "float" to "0",
    "float[]" to "null",
    "double" to "0",
    "double[]" to "null"
)

fun String.isDouble(): Boolean = decimalRegex.matches(this)

fun String.isInteger(): Boolean = integerRegex.matches(this)

fun String.isBoolean(): Boolean = booleanRegex.matches(this)

fun String.isClassPrimitive(): Boolean = primitiveClasses.containsKey(this)

fun String.toSimpleName(): String = split(".").last()

fun String.toKotlinClassName(): String {
    var result: String = toUpperCaseFirst(1)
    if (!result.endsWith("Kt")) {
        result += "Kt"
    }
    return result
}

val String.packageId: String
    get() {
        val lastIndexOfDot = lastIndexOf(".")
        if (lastIndexOfDot == -1) return ""
        return substring(0, lastIndexOfDot)
    }
