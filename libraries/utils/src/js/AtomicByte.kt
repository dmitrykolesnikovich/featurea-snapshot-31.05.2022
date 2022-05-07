package featurea

actual class AtomicByte {

    private var value: Byte = 0

    actual fun incrementAndGet(): Byte {
        value++
        return value
    }

    actual fun setValue(value: Byte) {
        this.value = value
    }

}