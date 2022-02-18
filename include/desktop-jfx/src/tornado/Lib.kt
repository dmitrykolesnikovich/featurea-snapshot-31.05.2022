package featurea.desktop.jfx

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

inline fun <T> ChangeListener(crossinline listener: (observable: ObservableValue<out T>?, oldValue: T, newValue: T) -> Unit): ChangeListener<T> =
    javafx.beans.value.ChangeListener<T> { observable, oldValue, newValue -> listener(observable, oldValue, newValue) }

inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach(action)

inline fun <T, R> Iterable<T>.mapEach(action: T.() -> R) = map(action)

fun <T> ObservableValue<T>.onChange(op: (T) -> Unit) {
    addListener { o, oldValue, newValue -> op(newValue) }
}

fun <T> ObservableValue<T>.onInit(init: (T) -> Unit) {
    addListener { o, oldValue, newValue ->
        if (oldValue == null && newValue != null) {
            init(newValue)
        }
    }
}

fun <T> ObservableValue<T>.onChangeTimes(times: Int, op: (T?) -> Unit) {
    var counter = 0
    val listener = object : ChangeListener<T> {
        override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
            if (++counter == times) {
                removeListener(this)
            }
            op(newValue)
        }
    }
    addListener(listener)
}

fun <T> ObservableValue<T>.onChangeOnce(op: (T?) -> Unit) {
    onChangeTimes(1, op)
}

fun <T> ObservableList<T>.onChange(op: (ListChangeListener.Change<out T>) -> Unit) = apply {
    addListener(ListChangeListener { op(it) })
}
