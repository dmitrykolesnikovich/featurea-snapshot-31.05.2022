package featurea.utils

interface EventListener {
    fun acceptEvent(): Boolean = true
}

inline fun <T : EventListener> Iterable<T>.forEachEvent(action: (T) -> Unit) {
    for (listener in this) if (listener.acceptEvent()) action(listener)
}

inline fun <T : EventListener> Iterable<T>.acceptFirst(predicate: (T) -> Boolean) {
    for (listener in this) if (listener.acceptEvent() && predicate(listener)) break
}
