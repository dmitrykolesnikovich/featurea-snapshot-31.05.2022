package featurea.input

import featurea.EventListener

interface InputListener : EventListener {
    fun happen(event: InputEvent) {} // todo rename to `accept`
    fun update(event: InputEvent)
}

fun InputListener(update: (event: InputEvent) -> Unit): InputListener = object : InputListener {
    override fun update(event: InputEvent) = update(event)
}
