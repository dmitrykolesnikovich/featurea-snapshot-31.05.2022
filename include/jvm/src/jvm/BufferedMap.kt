package featurea.jvm

import java.util.*

class BufferedMap<K, V> {

    private var frontBuffer: MutableMap<K, V> = LinkedHashMap()
    private var backBuffer: MutableMap<K, V> = LinkedHashMap()

    @Synchronized
    fun put(key: K, value: V): V? {
        return backBuffer.put(key, value)
    }

    @Synchronized
    fun putAll(map: Map<K, V>?) {
        backBuffer.putAll(map!!)
    }

    @Synchronized
    fun remove(key: K): V? {
        return backBuffer.remove(key)
    }

    @Synchronized
    fun getFrontBuffer(): Map<K, V> {
        return frontBuffer
    }

    @Synchronized
    fun swapBuffers(): MutableMap<K, V> {
        val temp = frontBuffer
        frontBuffer = backBuffer
        backBuffer = temp
        return frontBuffer
    }

    @Synchronized
    fun clear() {
        frontBuffer.clear()
        backBuffer.clear()
    }

    @Synchronized
    fun sync(): Map<K, V> {
        frontBuffer.putAll(backBuffer)
        backBuffer.clear()
        return frontBuffer
    }

}