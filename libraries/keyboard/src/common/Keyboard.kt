package featurea.keyboard

import featurea.ApplicationController
import featurea.BufferedList
import featurea.math.Size
import featurea.runtime.Module
import featurea.utils.Property
import featurea.runtime.import

class Keyboard(module: Module) : ApplicationController(module) {

    private val delegate: KeyboardDelegate = import()

    private val events = BufferedList<KeyEvent>()
    private val keyListeners = mutableListOf<KeyListener>()
    private val keyboardListeners = mutableListOf<KeyboardListener>()
    val size: Size = Size()
    val isVisibleProperty = Property<Boolean>(value = false)
    var isVisible: Boolean by isVisibleProperty
        internal set

    fun addListener(keyListener: (keyEvent: KeyEvent) -> Unit) {
        addListener(KeyListener(keyListener))
    }

    fun addListener(keyListener: KeyListener) {
        keyListeners.add(keyListener)
    }

    fun addListener(keyboardListener: KeyboardListener) {
        keyboardListeners.add(keyboardListener)
    }

    fun removeListener(keyListener: KeyListener) {
        keyListeners.remove(keyListener)
    }

    fun removeListener(keyboardListener: KeyboardListener) {
        keyboardListeners.remove(keyboardListener)
    }

    fun addEvent(keyEvent: KeyEvent) {
        events.add(keyEvent)
    }

    override suspend fun update() {
        val eventsFrontBuffer = events.swap()
        if (isEnable) {
            for (keyEvent in eventsFrontBuffer) {
                for (listener in keyListeners) {
                    if (listener.acceptEvent()) {
                        listener.onKey(keyEvent)
                    }
                }
            }
        }
        eventsFrontBuffer.clear()
    }

    fun show(keyboardType: KeyboardType) {
        delegate.show(keyboardType)
    }

    fun hide() {
        delegate.hide()
    }

    /*internals*/

    internal fun fireShowKeyboard(width: Int, height: Int) {
        isVisible = true
        size.assign(width.toFloat(), height.toFloat())
        for (keyboardListener in keyboardListeners) {
            keyboardListener.onShowKeyboard(width.toFloat(), height.toFloat())
        }
    }

    internal fun fireHideKeyboard() {
        isVisible = false
        size.clear()
        for (keyboardListener in keyboardListeners) {
            keyboardListener.onHideKeyboard()
        }
    }

}
