package featurea.keyboard

import featurea.EventListener

interface KeyboardListener : EventListener {
    fun onShowKeyboard(width: Float, height: Float) {}
    fun onHideKeyboard() {}
}
