package featurea.js

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Window

// >> https://stackoverflow.com/a/11284322/909169
private const val IPHONE_KEYBOARD_HEIGHT_PORTRAIT: Int = 216
private const val IPHONE_KEYBOARD_HEIGHT_LANDSCAPE: Int = 162
// <<

val Window.isMobileKeyboardVisible: Boolean
    get() {
        if (!isUserAgentMobile) {
            return false
        }
        val keyboardHeight: Int = keyboardHeight
        val result: Boolean = keyboardHeight > 144 * devicePixelRatio
        println("keyboardHeight: $keyboardHeight")
        println("isMobileKeyboardVisible: $result")
        return result
    }

val Window.keyboardHeight: Int
    get() {
        if (!isUserAgentMobile) {
            return 0
        }
        val devicePixelRatio = devicePixelRatio.toInt()
        val mainCanvas: Element? = document.querySelector("#mainCanvas")
        val titleBarHeight: Int = if (mainCanvas is HTMLElement) {
            mainCanvas.parentElement?.parentElement?.querySelector(".titlebar")?.clientHeight ?: 0
        } else {
            0
        }
        val result: Int = (CURRENT_HEIGHT + titleBarHeight - innerHeight) * devicePixelRatio
        println("currentHeight: $CURRENT_HEIGHT")
        println("titleBarHeight: $titleBarHeight")
        println("innerHeight: $innerHeight")
        println("devicePixelRatio: $devicePixelRatio")
        println("keyboardHeight: $result")
        return result
    }

val Window.keyboardWidth: Int
    get() {
        if (!isUserAgentMobile) {
            return 0
        }
        val devicePixelRatio = devicePixelRatio.toInt()
        val result: Int = CURRENT_WIDTH * devicePixelRatio
        println("currentWidth: $CURRENT_WIDTH")
        println("devicePixelRatio: $devicePixelRatio")
        println("keyboardWidth: $result")
        return result
    }
