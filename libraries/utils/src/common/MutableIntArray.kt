@file:Suppress("EqualsOrHashCode")

package featurea.utils

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

class MutableIntArray : Iterable<Int> {

    var items: IntArray
    var size = 0

    @JvmOverloads
    constructor(capacity: Int = 16) {
        items = IntArray(capacity)
    }

    constructor(array: MutableIntArray) {
        size = array.size
        items = IntArray(size)
        array.items.copyInto(items) // System.arraycopy(array.items, 0, items, 0, size)
    }

    fun add(value: Int) {
        var items = items
        if (size == items.size) items = resize(max(8, (size * 1.75f).toInt()))
        items[size++] = value
    }

    operator fun get(index: Int): Int {
        if (index >= size) throw IndexOutOfBoundsException("index can't be >= size: $index >= $size")
        return items[index]
    }

    fun removeIndex(index: Int): Int {
        if (index >= size) throw IndexOutOfBoundsException("index can't be >= size: $index >= $size")
        val items = items
        val value = items[index]
        size--
        items.copyInto(items, index, index + 1, size) // System.arraycopy(items, index + 1, items, index, size - index)
        return value
    }

    fun pop(): Int {
        return items[--size]
    }

    fun clear() {
        size = 0
    }

    fun ensureCapacity(additionalCapacity: Int): IntArray {
        val sizeNeeded = size + additionalCapacity
        if (sizeNeeded > items.size) resize(max(8, sizeNeeded))
        return items
    }

    private fun resize(size: Int): IntArray {
        val newSize: Int = min(this.size, size)
        val newItems: IntArray = IntArray(size)
        items.copyInto(newItems, 0, 0, newSize) // System.arraycopy(items, 0, newItems, 0, min(size, newItems.size))
        items = newItems
        return items
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MutableIntArray) return false
        if (size != other.size) return false
        for (index in 0 until size) {
            if (items[index] != other.items[index]) {
                return false
            }
        }
        return true
    }

    override fun iterator(): Iterator<Int> = items.iterator()

}
