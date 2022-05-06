package featurea

import featurea.utils.splitAndTrim

// 0.0..10.0 <=> FloatRange(first = 0f, last = 10f)
fun String.toClosedFloatingPointRange(): ClosedFloatingPointRange<Float> {
    val (first, second) = split("..")
    return first.toFloat()..second.toFloat()
}

class FloatRange(var first: Float, var last: Float) {
    val delta: Float get() = last - first
    fun contains(value: Float) = value in first..last
    override fun toString() = "${first}..${last}"
}

fun ClosedFloatingPointRange<Float>.toFloatRange(): FloatRange = FloatRange(start, endInclusive)

data class MutablePair<A, B>(var first: A, var second: B)

infix fun <A, B> A.mto(that: B): MutablePair<A, B> = MutablePair(this, that)

val <K> MutablePair<K, *>.key: K
    get() = first

val <V> MutablePair<*, V>.value: V
    get() = second

inline fun <reified V : Any> String.toPair(delimiter: String): Pair<String, V> {
    val tokens = splitAndTrim(delimiter, 2)
    if (tokens.size == 2) {
        return Pair(tokens[0], tokens[1].castTo())
    } else {
        return Pair(tokens[0], "".castTo())
    }
}

inline fun <reified T : Any> String.castTo(): T {
    return when (T::class) {
        String::class -> this
        Float::class -> this.toFloat()
        else -> TODO()
    } as T
}

fun String.toPair(delimiter: String, delimiterPattern: String): Pair<String, String?> {
    val regex = delimiterPattern.toRegex()
    val matchResult = regex.find(this)
    if (matchResult != null) {
        val delimiterIndex = matchResult.value.indexOf(delimiter)
        val index1 = matchResult.range.first + delimiterIndex - 1
        val index2 = index1 + delimiter.length + 1
        val first = substring(0..index1)
        val second = substring(index2)
        return Pair(first, second)
    } else {
        return Pair(this, null)
    }
}

fun String.divide(index: Int) = Pair(substring(0, index), substring(index + 1))

fun String.divide(delimiter: String, defaultSecondValue: () -> String? = { null }): Pair<String, String?> {
    val tokens = splitAndTrim(delimiter, 2)
    if (tokens.size == 2) {
        return Pair(tokens[0], tokens[1])
    } else {
        return Pair(tokens[0], defaultSecondValue())
    }
}