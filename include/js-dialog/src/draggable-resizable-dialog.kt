package featurea.js.dialog

import featurea.js.*
import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.dom.append
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlinx.browser.window as jsWindow

val HTMLElement.titlebarOrNull: HTMLElement? get() = querySelector(".titlebar") as HTMLElement?
val HTMLElement.titlebarHeight: Int get() = titlebarOrNull?.clientHeight ?: 0 // 32px

fun showDialogBox(dialogId: String) {
    js("new window.DialogBox(dialogId).showDialog()")
}

fun registerEscapeListener(rootElements: Map<String, HTMLElement>) {
    fun callback(event: Event) {
        // >> quickfix todo improve
        val target: HTMLElement? = event.target as HTMLElement?
        if (target != null) {
            if (target.classList.asList().find { it.startsWith("mdi-") } != null) return
            if (target.containsRecursively { it.classList.asList().find { it == "timepicker_wrap" } != null }) return
        }
        // <<

        val escapeTolerance = 8
        val touch = if (event is TouchEvent) event.changedTouches[0]!! else null
        val x = touch?.clientX ?: if (event is MouseEvent) event.clientX else -1
        val y = touch?.clientY ?: if (event is MouseEvent) event.clientY else -1
        for ((name, rootElement) in rootElements) {
            if (rootElement.style.display == "none") continue
            if (rootElement.getBoundingClientRect().contains(x, y, escapeTolerance)) return
            val mainCanvas: Element = rootElement.querySelector("#mainCanvas") ?: error("rootElement: $rootElement")
            if (mainCanvas.getBoundingClientRect().contains(x, y, escapeTolerance)) return
        }
        for ((name, rootElement) in rootElements) {
            rootElement.style.applyCssAttribute("display" to "none")
        }
    }
    document.addEventListener("touchstart", ::callback)
    document.addEventListener("mousedown", ::callback)
}

fun HTMLElement.appendMainCanvasDialog(title: String, screenRatio: Float, splashImagePath: String?): HTMLElement {
    val dialog = append.div {
        classes += "dialog"
        div {
            classes += "titlebar"
            +title
        }
        button {
            id = "closeButton"
            +"✖"
        }
        div {
            classes += "mainCanvasDialogContent"
            canvas {
                id = "mainCanvas"
                style = "display: hidden;"
            }
        }
        div {
            id = "loaderLabel"
            classes += "content"
            classes += "input-disable"
            style = "display: none; color: white; vertical-align: middle; text-align: center;"
            +"Loading 0%"
        }
        div {
            classes += "content"
            classes += "input-disable"
            style =
                "left: 0px; top: 0px; width: 100%; height: 100%; text-align: center; background-color: white; display: flex; align-items: center; justify-content: center;"

            img {
                id = "splashImage"
                style = "display: none; height: 100%; width: auto;"
                src = splashImagePath ?: "_"
            }

            img {
                id = "loadingImage"
                style = "display: none; width: 200px; height: 200px; background-color: white"
                src = "images/Loading.gif"
            }
        }
        /*input {
            id = "hiddenInput"
            type = InputType.text
        }*/
    }
    val height = jsWindow.innerHeight * .8f
    if (isUserAgentMobile) {
        dialog.style.width = "100%"
        dialog.style.height = "100%"
    } else {
        dialog.style.width = "${screenRatio * height}px"
        dialog.style.height = "${height + 30f}px" // quickfix todo delete `+ 30f`
    }
    dialog.style.zIndex = "6"

    (dialog.querySelector("#loaderLabel") as HTMLElement).style.applyCssAttribute("line-height" to "${height - 80}px")
    return dialog
}

fun HTMLElement.onChangeCssProperty(key: String, listener: (value: String) -> Unit) {
    val currentKey = "$key-current"
    val value = style.getPropertyValue(key)
    style.setProperty(currentKey, value)
    jsWindow.setTimeout({
        val currentValue = style.getPropertyValue(currentKey)
        val value = style.getPropertyValue(key)
        if (currentValue != value) {
            style.setProperty(currentKey, value)
            listener(value)
        }
    }, 300)
}

fun HTMLElement.setupFullScreenSize() {
    // >> quickfix todo improve
    if (jsWindow.isMobileKeyboardVisible) return
    val title: HTMLElement = querySelector(".titlebar") as HTMLElement? ?: return
    // <<

    val width = jsWindow.innerWidth
    val height = jsWindow.innerHeight
    println("setupFullScreenSize: $width, $height")
    style.left = "0px"
    style.top = "0px"
    style.width = "${width}px"
    style.height = "${height}px"
    style.zIndex = "5"
    style.position = "fixed"

    (querySelector("#loaderLabel") as HTMLElement?)?.style?.applyCssAttribute("line-height" to "${jsWindow.innerHeight - 80}px")
    title.style.width = "${jsWindow.innerWidth - 8}px"
}
