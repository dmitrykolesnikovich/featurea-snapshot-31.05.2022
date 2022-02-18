package featurea.jvm

class BufferedList<T : Any?> {

    private var frontBuffer: MutableList<T> = mutableListOf()
    private var backBuffer: MutableList<T> = mutableListOf()

    @Synchronized
    fun add(element: T): Boolean {
        return backBuffer.add(element)
    }

    @Synchronized
    fun addAll(elements: List<T>): Boolean {
        return backBuffer.addAll(elements)
    }

    @Synchronized
    fun swap(): MutableList<T> {
        with(backBuffer) {
            backBuffer = frontBuffer
            frontBuffer = this@with
        }
        return frontBuffer
    }

    @Synchronized
    fun clear() {
        frontBuffer.clear()
        backBuffer.clear()
    }

}
