package featurea.keyboard

import featurea.utils.EventListener

interface KeyboardListener : EventListener {
    fun onShowKeyboard(width: Float, height: Float) {}
    fun onHideKeyboard() {}
}
