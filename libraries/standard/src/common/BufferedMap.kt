package featurea

expect class BufferedMap<K, V>() {
    fun put(key: K, value: V): V?
    fun remove(key: K): V?
    fun getFrontBuffer(): Map<K, V>
    fun swapBuffers(): MutableMap<K, V>
    fun clear()
    fun sync(): Map<K, V>
}