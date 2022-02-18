package featurea.js

import kotlinx.browser.document
import org.w3c.dom.*
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.browser.window as jsWindow
import org.w3c.dom.Window as JsWindow

val isUserAgentMobile: Boolean = jsWindow.navigator.userAgent.toLowerCase().let {
    it.contains("mobi") || it.contains("tab") || it.contains("ios") || it.contains("android")
}

val isUserAgentNotMobile: Boolean = !isUserAgentMobile

val isUserAgentAndroid: Boolean get() = jsWindow.navigator.userAgent.toLowerCase().contains("android")

val isUserAgentIphone: Boolean get() = jsWindow.navigator.userAgent.toLowerCase().contains("iphone")

// https://stackoverflow.com/a/40130925/909169
val isPortraitOrientation: Boolean
    get() {
        if (isUserAgentAndroid) {
            return jsWindow.screen.height > jsWindow.screen.width
        } else {
            return jsWindow["orientation"] == 0
        }
    }

val isLandscapeOrientation: Boolean get() = !isPortraitOrientation

// >> just for now todo revert
val CURRENT_WIDTH: Int
    get() {
        if (isUserAgentMobile) {
            if (isUserAgentAndroid) {
                return fullscreenWidthOrNull ?: jsWindow./*outerWidth*/screen.width
            } else {
                if (isPortraitOrientation) {
                    return fullscreenWidthOrNull ?: jsWindow./*outerWidth*/screen.width
                } else {
                    return fullscreenHeightOrNull ?: jsWindow./*outerHeight*/screen.height
                }
            }
        } else {
            return fullscreenWidthOrNull ?: jsWindow./*outerWidth*/screen.width
        }
    }

val fullscreenHeightOrNull: Int? get() = document.fullscreenElement?.clientHeight
val fullscreenWidthOrNull: Int? get() = document.fullscreenElement?.clientWidth

val CURRENT_HEIGHT: Int
    get() {
        if (isUserAgentMobile) {
            val mainCanvas: Element? = document.querySelector("#mainCanvas") // quickfix todo improve
            if (mainCanvas is HTMLElement) {
                val style: CSSStyleDeclaration = mainCanvas.style
                if (isUserAgentAndroid) {
                    println("[JsUtils] outerHeight: ${jsWindow.outerHeight}")
                    println("[JsUtils] style.height: ${style.height}")
                    val result: Int = fullscreenHeightOrNull ?: if (isPortraitOrientation) {
                        style.height.pxToInt(jsWindow.outerHeight)
                    } else {
                        min(style.height.pxToInt(jsWindow.outerHeight), jsWindow.outerHeight)
                    }
                    println("[Support] CURRENT_HEIGHT: $result (android)")
                    return result
                } else {
                    if (isPortraitOrientation) {
                        val result: Int = fullscreenHeightOrNull ?: min(style.height.pxToInt(jsWindow.outerHeight), jsWindow.outerHeight)
                        println("[Support] CURRENT_HEIGHT: $result (ios)")
                        return result
                    } else {
                        val result: Int =
                            fullscreenWidthOrNull ?: min(style.width.pxToInt(jsWindow.outerWidth), jsWindow.outerWidth)
                        println("[Support] CURRENT_HEIGHT: $result (ios)")
                        return result
                    }
                }
            }
        }
        return fullscreenHeightOrNull ?: jsWindow.outerHeight
    }
// <<

fun CSSStyleDeclaration.applyCssAttribute(attribute: Pair<String, String>) {
    cssText = cssText.plus(" ${attribute.first}: ${attribute.second};")
}

fun DOMRect.contains(x: Int, y: Int, epsilon: Int = 0): Boolean {
    return left - epsilon <= x && right + epsilon >= x && top - epsilon <= y && bottom + epsilon >= y
}

fun WebSocket.onopen(timeout: Int, block: () -> Unit) {
    onopen = {
        block()
    }
    jsWindow.setTimeout({
        if (readyState != WebSocket.OPEN) {
            close()
        }
    }, timeout)
}

fun WebSocket.closeWithoutEvents() {
    onmessage = {}
    onclose = {}
    onerror = {}
    if (readyState == WebSocket.OPEN || readyState == WebSocket.CONNECTING) {
        close()
    }
}

// https://stackoverflow.com/a/39276894/909169
/**
 * @return glWidth and glHeight
 */
fun HTMLCanvasElement.setHtmlCanvasSize(width: Int, height: Int): Pair<Int, Int> {
    val glWidth = (width * jsWindow.devicePixelRatio).roundToInt()
    val glHeight = (height * jsWindow.devicePixelRatio).roundToInt()
    println("setHtmlCanvasSize: $width, $height, $glWidth, $glHeight (${fullscreenHeightOrNull}, ${document.fullscreenElement == this})")
    this.style.width = "${width}px"
    this.style.height = "${height}px"
    this.width = glWidth
    this.height = glHeight
    return glWidth to glHeight
}

val HTMLElement.glWidth: Int get() = (clientWidth * jsWindow.devicePixelRatio).roundToInt() // https://www.khronos.org/webgl/wiki/HandlingHighDPI

val HTMLElement.glHeight: Int get() = (clientHeight * jsWindow.devicePixelRatio).roundToInt() // https://www.khronos.org/webgl/wiki/HandlingHighDPI

fun HTMLElement.setSize(width: Int, height: Int) {
    this.style.width = "${width}px"
    this.style.height = "${height}px"
}

fun HTMLElement.setPosition(x: Int, y: Int) {
    this.style.left = "${x}px"
    this.style.top = "${y}px"
}

fun String.pxToInt(default: Int): Int {
    if (this.isBlank()) return default
    try {
        return removeSuffix("px").toInt()
    } catch (e: Exception) {
        return default
    }
}

fun JsWindow.loadBody(block: () -> Unit) {
    if (document.readyState == DocumentReadyState.COMPLETE) {
        block()
    } else {
        onload = { block() }
    }
}

fun keep(ref: Any?) {
    // no op
}

fun Element.containsRecursively(predicate: (Element) -> Boolean): Boolean {
    var currentElement: Element? = this
    while (true) {
        if (currentElement == null) return false
        if (predicate(currentElement)) return true
        currentElement = currentElement.parentElement
    }
}

fun export(key: String, block: dynamic) {
    jsWindow.asDynamic()[key] = block
}
