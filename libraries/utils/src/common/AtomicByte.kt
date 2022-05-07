package featurea.utils

expect class AtomicByte() {
    fun incrementAndGet(): Byte
    fun setValue(value: Byte)
}