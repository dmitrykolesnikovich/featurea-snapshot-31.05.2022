package featurea

// quickfix todo improve
// it's not thread safe but it solves "modifying while iterating" issue
// for now used by Application.tasks, Input.events and Keyboard.events
class BufferedList<T : Any?> {

    private var frontBuffer: MutableList<T> = mutableListOf()
    var backBuffer: MutableList<T> = mutableListOf()
        private set

    fun add(element: T): Boolean {
        return backBuffer.add(element)
    }

    fun addAll(elements: List<T>): Boolean {
        return backBuffer.addAll(elements)
    }

    fun swap(): MutableList<T> {
        with(backBuffer) {
            backBuffer = frontBuffer
            frontBuffer = this@with
        }
        return frontBuffer
    }

    // >>  quickfix todo improve
    fun poll(action: (oldBuffer: List<T>, newBuffer: List<T>) -> Unit) {
        try {
            swap()
            action(frontBuffer, ArrayList(backBuffer))
        } finally {
            frontBuffer.clear()
        }
    }
    // <<

    fun clear() {
        frontBuffer.clear()
        backBuffer.clear()
    }

}
