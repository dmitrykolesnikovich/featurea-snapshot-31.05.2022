package featurea.keyboard

import featurea.EventListener
import featurea.runtime.Constructor

fun interface KeyListener : EventListener {
    fun onKey(keyEvent: KeyEvent)
}

