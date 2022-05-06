package featurea.keyboard

import featurea.utils.breakpoint
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.EventListener as JsEventListener

private const val KEY_UP = "keyup"

// https://bugs.chromium.org/p/chromium/issues/detail?id=118639
actual class KeyEventProducer actual constructor(override val module: Module) : Component, JsEventListener {

    private val delegate: KeyboardDelegate = import()
    private val keyboard: Keyboard = import()

    override fun onCreateComponent() {
        delegate.hiddenInput.addEventListener(KEY_UP, this)
    }

    override fun onDeleteComponent() {
        delegate.hiddenInput.removeEventListener(KEY_UP, this)
    }

    override fun handleEvent(event: Event) {
        event as KeyboardEvent

        when (event.type) {
            KEY_UP -> {
                var source: KeyEventSource? = event.findKeyEventSourceOrNull()
                if (source == null) {
                    breakpoint()
                    val lastChar = delegate.hiddenInput.value.last()
                    breakpoint()
                    source = lastChar.toKeyEventSource()
                    breakpoint()
                }
                keyboard.addEvent(KeyEvent(KeyEventType.PRESS, source))
                keyboard.addEvent(KeyEvent(KeyEventType.RELEASE, source))
                keyboard.addEvent(KeyEvent(KeyEventType.CLICK, source))
            }
        }
    }

}
