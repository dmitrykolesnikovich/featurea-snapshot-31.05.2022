// https://gist.github.com/dmitrykolesnikovich/79ffee31f319ca01f84f6b69d9d92f02
package featurea

import kotlin.jvm.Transient

private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

private fun hugeCapacity(minCapacity: Int): Int {
    when {
        minCapacity < 0 -> error("out of memory")
        minCapacity > MAX_ARRAY_SIZE -> return Int.MAX_VALUE
        else -> return MAX_ARRAY_SIZE
    }
}

@Suppress("UNCHECKED_CAST")
class PriorityQueue<T>(initialCapacity: Int = 11) {

    @Transient
    var queue: Array<T?>

    var size: Int = 0

    @Transient
    var modCount = 0

    init {
        check(initialCapacity > 0)
        queue = arrayOfNulls<Any>(initialCapacity) as Array<T?>
    }

    fun add(element: T): Boolean {
        return offer(element)
    }

    fun remove(element: T): Boolean {
        val index: Int = indexOf(element)
        if (index == -1) return false
        removeAt(index)
        return true
    }

    fun offer(e: T): Boolean {
        modCount++
        val size: Int = size
        if (size >= queue.size) grow(size + 1)
        this.size = size + 1
        if (size == 0) queue[0] = e else siftUp(size, e)
        return true
    }

    fun peek(): T? {
        if (size == 0) return null
        return queue[0]
    }

    fun poll(): T? {
        if (size == 0) return null
        val s: Int = --size
        modCount++
        val result: T? = queue[0]
        val x: T? = queue[s]
        queue[s] = null
        if (s != 0) {
            siftDown(0, x)
        }
        return result
    }

    operator fun contains(element: T): Boolean {
        return indexOf(element) != -1
    }

    fun clear() {
        modCount++
        for (index in 0 until size) {
            queue[index] = null
        }
        size = 0
    }

    fun isEmpty(): Boolean {
        return size == 0
    }

    /*internals*/

    private fun grow(minCapacity: Int) {
        val oldCapacity: Int = queue.size
        var newCapacity: Int = oldCapacity + if (oldCapacity < 64) oldCapacity + 2 else oldCapacity shr 1
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity)
        }
        queue = queue.copyOf(newCapacity)
    }

    private fun removeAt(i: Int): T? {
        modCount++
        val s: Int = --size
        if (s == i) {
            queue[i] = null
        } else {
            val moved: T? = queue[s]
            queue[s] = null
            siftDown(i, moved)
            if (queue[i] === moved) {
                siftUp(i, moved)
                if (queue[i] !== moved) {
                    return moved
                }
            }
        }
        return null
    }

    private fun siftUp(k: Int, x: T?) {
        siftUpComparable(k, x)
    }

    private fun siftUpComparable(k: Int, x: T?) {
        var k: Int = k
        val key = x as Comparable<T?>?
        while (k > 0) {
            val parent: Int = k - 1 ushr 1
            val e = queue[parent]
            if (key!! >= e) break
            queue[k] = e
            k = parent
        }
        queue[k] = key as T?
    }

    private fun siftDown(k: Int, x: T?) {
        siftDownComparable(k, x)
    }

    private fun siftDownComparable(k: Int, x: T?) {
        var k: Int = k
        val key = x as Comparable<T?>?
        val half = size ushr 1 // loop while a non-leaf
        while (k < half) {
            var child: Int = (k shl 1) + 1 // assume left child is least
            var c: T? = queue[child]
            val right: Int = child + 1
            if (right < size && (c as Comparable<T?>?)!! > queue[right]) {
                c = queue[right.also { child = it }]
            }
            if (key!! <= c) break
            queue[k] = c
            k = child
        }
        queue[k] = key as T?
    }

    private fun indexOf(value: T): Int {
        if (value != null) {
            for (index in 0 until size) {
                if (value == queue[index]) {
                    return index
                }
            }
        }
        return -1
    }

}
