package featurea.input

import featurea.app.ApplicationController
import featurea.utils.BufferedList
import featurea.runtime.Module

class Input(module: Module) : ApplicationController(module) {

    private val events = BufferedList<InputEvent>()
    private val listeners = mutableListOf<InputListener>()

    fun addListener(inputListener: InputListener) {
        listeners.add(inputListener)
    }

    fun addListener(update: (event: InputEvent) -> Unit) {
        listeners.add(InputListener(update))
    }

    fun removeListener(inputListener: InputListener) {
        listeners.remove(inputListener)
    }

    fun addEvent(event: InputEvent) {
        events.add(event)
        for (listener in listeners) {
            listener.happen(event)
        }
    }

    override suspend fun update() {
        val eventsFrontBuffer = events.swap()
        if (isEnable) {
            for (inputEvent in eventsFrontBuffer) {
                for (listener in listeners) {
                    if (listener.acceptEvent()) {
                        listener.update(inputEvent)
                    }
                }
            }
        }
        eventsFrontBuffer.clear()
    }

}
