package featurea.keyboard

import featurea.utils.EventListener

fun interface KeyListener : EventListener {
    fun onKey(keyEvent: KeyEvent)
}

