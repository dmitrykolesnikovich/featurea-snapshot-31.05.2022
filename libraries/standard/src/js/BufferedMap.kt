package featurea

actual class BufferedMap<K, V> {
    actual fun put(key: K, value: V): V? = TODO()
    actual fun remove(key: K): V? = TODO()
    actual fun getFrontBuffer(): Map<K, V> = TODO()
    actual fun swapBuffers(): MutableMap<K, V> = TODO()
    actual fun clear(): Unit = TODO()
    actual fun sync(): Map<K, V> = TODO()
}
