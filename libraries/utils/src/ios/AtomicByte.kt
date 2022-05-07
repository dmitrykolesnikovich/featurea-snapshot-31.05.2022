package featurea

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

actual class AtomicByte : SynchronizedObject() {

    private var value: Byte = 0

    actual fun incrementAndGet(): Byte {
        synchronized(this) {
            value++
            return value
        }
    }

    actual fun setValue(value: Byte) {
        synchronized(this) {
            this.value = value
        }
    }

}
