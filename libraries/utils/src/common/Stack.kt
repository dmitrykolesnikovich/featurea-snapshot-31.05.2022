package featurea.utils

open class Stack<T> {

    private val data: MutableList<T> = mutableListOf()

    constructor()

    constructor(original: Collection<T>) {
        data.addAll(original)
    }

    constructor(vararg original: T) {
        data.addAll(original)
    }

    fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return data.isNotEmpty()
    }

    fun push(element: T) {
        data.add(element)
    }

    fun pop(): T {
        val element: T = data.removeAt(data.lastIndex)
        return element
    }

    fun popOrNull(): T? {
        if (data.isEmpty()) return null
        return pop()
    }

    fun last(): T {
        return data[data.lastIndex]
    }

    fun lastOrNull(): T? {
        if (isEmpty()) {
            return null
        }
        return data[data.lastIndex]
    }

    val size: Int get() = data.size

    fun clear() {
        data.clear()
    }

}