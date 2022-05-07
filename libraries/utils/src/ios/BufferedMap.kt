package featurea.utils

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

actual class BufferedMap<K, V> {

    private var frontBuffer: MutableMap<K, V> = LinkedHashMap()
    private var backBuffer: MutableMap<K, V> = LinkedHashMap()
    private val lockObject = SynchronizedObject()

    actual fun put(key: K, value: V): V? {
        initRuntimeIfNeeded()
        synchronized(lockObject) {
            return backBuffer.put(key, value)
        }
    }

    actual fun remove(key: K): V? {
        initRuntimeIfNeeded()
        synchronized(lockObject) {
            return backBuffer.remove(key)
        }
    }

    actual fun getFrontBuffer(): Map<K, V> {
        initRuntimeIfNeeded()
        synchronized(lockObject) {
            return frontBuffer
        }
    }

    actual fun swapBuffers(): MutableMap<K, V> {
        initRuntimeIfNeeded()
        synchronized(lockObject) {
            val temp = frontBuffer
            frontBuffer = backBuffer
            backBuffer = temp
            return frontBuffer
        }
    }

    actual fun clear() {
        initRuntimeIfNeeded()
        synchronized(lockObject) {
            frontBuffer.clear()
            backBuffer.clear()
        }
    }

    actual fun sync(): Map<K, V> {
        initRuntimeIfNeeded()
        synchronized(lockObject) {
            frontBuffer.putAll(backBuffer)
            backBuffer.clear()
            return frontBuffer
        }
    }

}