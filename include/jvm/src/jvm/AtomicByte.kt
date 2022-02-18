package featurea.jvm

class AtomicByte {

    @Volatile
    private var value: Byte = 0

    @Synchronized
    fun incrementAndGet(): Byte {
        value++
        return value
    }

    @Synchronized
    fun setValue(value: Byte) {
        this.value = value
    }

}