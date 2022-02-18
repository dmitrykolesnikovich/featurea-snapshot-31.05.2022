package featurea.window

import featurea.EventListener
import featurea.layout.Layout
import featurea.layout.Orientation

interface WindowListener : EventListener {
    fun init() {}
    fun resize(width: Int, height: Int) {}
    fun invalidate() {}
    fun updateLayout(layout: Layout) {}
    fun updateOrientation(orientation: Orientation) {}
}

fun WindowInitListener(block: () -> Unit) = object : WindowListener {
    override fun init() = block()
}

fun WindowInvalidateListener(task: () -> Unit): WindowListener = object : WindowListener {
    override fun invalidate() = task()
}
