@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package featurea.utils

fun List<Any?>.firstStringOrNull(): String? {
    val value: Any? = firstOrNull()
    return if (value is String) value else null
}

fun List<Any?>.firstString(): String {
    return firstStringOrNull() ?: error("list: ${joinToString()}")
}

infix fun List<String>.toMap(values: List<Any>): MutableMap<String, Any?> {
    require(size == values.size) { "keys size: $size and values size: ${values.size} are not the same" }
    val result = mutableMapOf<String, Any?>()
    for (index in 0..size) result[this[index]] = values[index]
    return result
}

fun <T : Any?> MutableCollection<T>.replaceWith(collection: Iterable<T>?): MutableCollection<T> {
    clear()
    if (collection != null) {
        for (element in collection) {
            add(element)
        }
    }
    return this
}

fun <K : Any, V : Any> MutableMap<K, V>.replaceWith(collection: Map<K, V>) {
    clear()
    for (element in collection) put(element.key, element.value)
}

fun <K : Any, T : Any> Map<K, T>.withIndex(): Iterable<IndexedValue<Map.Entry<K, T>>> {
    return object : Iterable<IndexedValue<Map.Entry<K, T>>> {
        override fun iterator(): Iterator<IndexedValue<Map.Entry<K, T>>> {
            return object : Iterator<IndexedValue<Map.Entry<K, T>>> {
                val iterator = entries.iterator()
                var index = 0
                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): IndexedValue<Map.Entry<K, T>> {
                    val nextValue = IndexedValue(index, iterator.next())
                    index++
                    return nextValue
                }

            }
        }
    }
}

fun <T> emptyIterator(): Iterator<T> = emptyList<T>().iterator()
fun <T> Iterator<T>.isEmpty(): Boolean = !hasNext()
fun <T> Iterator<T>.isNotEmpty(): Boolean = hasNext()
fun <T> MutableList<T>.removeFirst(): T = removeAt(0)
fun <T> List<T?>.contentEquals(other: List<T?>) = containsAll(other) && other.containsAll(this)
fun <T> List<T?>.contentNotEquals(other: List<T?>) = !contentEquals(other)

fun <T> List<T>.prelast(): T {
    return checkNotNull(prelastOrNull())
}

fun <T> List<T>.prelastOrNull(): T? {
    val index: Int = lastIndex - 1
    return if (index >= 0) this[index] else null
}

fun FloatArray.floatArraySizeInBytes(): Int = size * Float.SIZE_BYTES // IMPORTANT used by `GLES20.glBufferData()`

fun IntArray.intArraySizeInBytes(): Int = size * Int.SIZE_BYTES // IMPORTANT used by `GLES20.glBufferData()`

fun <T, R : Comparable<R>> LinkedHashSet<T>.sort(selector: (T) -> R?) {
    val sortedSet = sortedBy(selector)
    clear()
    addAll(sortedSet)
}

@OptIn(ExperimentalStdlibApi::class)
fun <T> ArrayDeque<T>.peek(): T? = firstOrNull()

@OptIn(ExperimentalStdlibApi::class)
fun <T> ArrayDeque<T>.poll(): T? = lastOrNull()

@OptIn(ExperimentalStdlibApi::class)
fun <T> ArrayDeque<T>.push(element: T) = addFirst(element)

@OptIn(ExperimentalStdlibApi::class)
fun <T> ArrayDeque<T>.pop() = removeFirst()

@OptIn(ExperimentalStdlibApi::class)
fun <T> ArrayDeque<T>.offer(element: T): Boolean = add(element)

fun Collection<Float>.findIndexBySum(sum: Float): Int {
    var currentSum = 0f
    for ((index, value) in withIndex()) {
        currentSum += value
        if (currentSum >= sum) return index
    }
    error("$sum > $currentSum")
}


@OptIn(ExperimentalStdlibApi::class)
fun <T> ArrayDeque<T>.popOrNull(): T? {
    if (isEmpty()) return null
    return pop()
}

fun List<*>.component1() = get(0)
fun List<*>.component2() = get(1)
fun List<*>.component3() = get(2)
fun List<*>.component4() = get(3)

fun String.toIntRange(): IntRange {
    val (from, to) = splitAndTrim("..")
    val intFrom: Int = from.toInt()
    val intTo: Int = to.toInt()
    return intFrom..intTo
}

data class IndexedEntry<out K, out V>(val index: Int, val key: K, val value: V)

fun <K : Any, V : Any> Map<K, V>.withIndexFlat(): Iterable<IndexedEntry<K, V>> {
    var index: Int = 0
    return map {
        IndexedEntry(index++, it.key, it.value)
    }
}


