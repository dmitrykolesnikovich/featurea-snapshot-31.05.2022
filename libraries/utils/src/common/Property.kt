package featurea.utils

import kotlin.reflect.KProperty

typealias PropertyWatcher<T> = (value: T?) -> Unit

class Property<T>(private var value: T? = null) {

    private val watchers = mutableListOf<PropertyWatcher<T>>()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value == null && value == null) return
        if (this.value != value) {
            this.value = value
            notifyWatchers()
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        val result: T = value as T
        return result
    }

    fun watch(watcher: PropertyWatcher<T>) {
        watchers.add(watcher)
    }

    fun watchBlocking(watcher: suspend () -> Unit) {
        watch {
            runBlocking {
                watcher()
            }
        }
    }

    fun notifyWatchers() {
        for (watcher in watchers) {
            try {
                watcher(value)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}
