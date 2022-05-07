package featurea.keyboard

import featurea.utils.isIphoneBrowser
import featurea.js.*
import featurea.utils.log
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.id
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlinx.browser.window as jsWindow

// https://css-tricks.com/everything-you-ever-wanted-to-know-about-inputmode
actual class KeyboardDelegate actual constructor(override val module: Module) : Component {

    private val keyboard: Keyboard = import()
    private val rootElement: HTMLElement = import(RootElementProxy)

    val hiddenInput: HTMLInputElement = rootElement.createInputElement { id = "hiddenInput"; type = InputType.text }
    private var intervalId: Int = -1
    private var isShown: Boolean = false

    init {
        hiddenInput.style.applyCssAttribute("type" to "text")
        hiddenInput.style.applyCssAttribute("position" to "fixed")
        hiddenInput.style.applyCssAttribute("left" to "100000px") // quickfix todo improve
        hiddenInput.style.applyCssAttribute("top" to "100000px") // quickfix todo improve
    }

    // https://stackoverflow.com/a/55652503/909169
    actual fun show(keyboardType: KeyboardType) {
        if (isShown) return
        isShown = true
        log("KeyboardDelegate.show: $hiddenInput")

        hiddenInput.style.display = "inline"
        hiddenInput.inputMode = keyboardType.toJsInputType()
        hiddenInput.focus()
        waitForRun {
            wait {
                jsWindow.keyboardHeight != 0
            }
            run {
                val keyboardWidth: Int = jsWindow.keyboardWidth
                val keyboardHeight: Int = jsWindow.keyboardHeight
                log("fireShowKeyboard: $keyboardWidth, $keyboardHeight")
                keyboard.fireShowKeyboard(keyboardWidth, keyboardHeight)
                startKeyboardVisibilityTimer()
            }
        }
    }

    actual fun hide() {
        if (!isShown) return
        isShown = false
        log("KeyboardDelegate.hide: $hiddenInput")

        // >> quickfix for todo improve
        println("[KeyboardDelegate] isIphoneBrowser: $isIphoneBrowser")
        println("[KeyboardDelegate] hiddenInput.inputMode: ${hiddenInput.inputMode}")
        if (isIphoneBrowser && hiddenInput.inputMode == "numeric") {
            println("[KeyboardDelegate] addEvent: ENTER")
            keyboard.addEvent(KeyEvent(type = KeyEventType.PRESS, source = KeyEventSource.ENTER))
            keyboard.addEvent(KeyEvent(type = KeyEventType.RELEASE, source = KeyEventSource.ENTER))
            keyboard.addEvent(KeyEvent(type = KeyEventType.CLICK, source = KeyEventSource.ENTER))
            GlobalScope.launch {
                println("[KeyboardDelegate] update")
                keyboard.update()
            }
        }
        // <<

        GlobalScope.launch {
            hiddenInput.blur()
            hiddenInput.style.display = "none"
            hiddenInput.value = ""
            stopKeyboardVisibilityTimer()
            println("[KeyboardDelegate] fireHideKeyboard")
            keyboard.fireHideKeyboard()
        }
    }

    private fun startKeyboardVisibilityTimer() {
        intervalId = jsWindow.setInterval({
            // filter
            if (!isUserAgentMobile) return@setInterval
            if (!isShown) return@setInterval

            // action
            if (!jsWindow.isMobileKeyboardVisible) {
                hide()
            }
        }, 300)
    }

    private fun stopKeyboardVisibilityTimer() {
        if (intervalId != -1) {
            jsWindow.clearInterval(intervalId)
            intervalId = -1
        }
    }

}

/*internals*/

private fun KeyboardType.toJsInputType(): String = when (this) {
    KeyboardType.TEXT -> "default"
    KeyboardType.NUMERIC -> "numeric"
}
