package featurea.window

import featurea.js.*
import featurea.js.dialog.titlebarHeight
import featurea.js.dialog.titlebarOrNull
import featurea.utils.log
import featurea.runtime.*
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Screen
import kotlinx.browser.window as jsWindow

@Provide(HtmlElementProxy::class)
class MainView(override val module: Module) : Component {

    private val rootElement: HTMLElement = import(RootElementProxy)
    private val window: Window = import()

    private val mainCanvas: HTMLCanvasElement = rootElement.querySelector("#mainCanvas") as HTMLCanvasElement
    private val dialogContent: HTMLElement? = rootElement.querySelector(".content") as HTMLElement?
    private val hasDialogContent: Boolean = dialogContent != null
    private var isDestroy: Boolean = false
    private var currentWidth: Int = -1
    private var currentHeight: Int = -1
    private var then: Float = 0f

    override fun onCreateComponent() {
        if (hasDialogContent) {
            mainCanvas.setHtmlCanvasSize(rootElement.clientWidth, rootElement.clientHeight - rootElement.titlebarHeight)
        }
        dialogContent?.onchange = /*draggable-resizable-dialog.js:228*/ {
            window.updateLayout()
        }
        provide(HtmlElementProxy(mainCanvas))
        jsWindow.requestAnimationFrame {
            onUpdate(it)
        }
        if (isUserAgentNotMobile) {
            val titlebar: HTMLElement? = rootElement.titlebarOrNull
            if (titlebar != null) {
                log("[MainView] titlebar: $titlebar")
                titlebar.addEventListener("dblclick", {
                    val mainCanvas: HTMLCanvasElement = rootElement.querySelector("#mainCanvas") as HTMLCanvasElement
                    keep(mainCanvas)
                    js(
                        """
                        if (mainCanvas.requestFullscreen) {
                            mainCanvas.requestFullscreen();
                        } else if (mainCanvas.mozRequestFullScreen) {
                            mainCanvas.mozRequestFullScreen();
                        } else if (mainCanvas.webkitRequestFullscreen) {
                            mainCanvas.webkitRequestFullscreen();
                        } else if (mainCanvas.msRequestFullscreen) {
                            mainCanvas.msRequestFullscreen();
                        }
                        """
                    )
                })
            }
        }
    }

    override fun onDeleteComponent() {
        isDestroy = true
    }

    /*internals*/

    private fun onUpdate(now: Double) {
        if (then == 0f) window.init() // not created yet
        if (isDestroy) return // already destroyed

        val now: Float = now.toFloat()
        val elapsedTime: Float = now - then
        then = now
        GlobalScope.launch {
            try {
                onResize()
                window.update(elapsedTime)
            } finally {
                jsWindow.requestAnimationFrame {
                    onUpdate(it)
                }
            }
        }
    }

    private fun onResize() {
        if (hasDialogContent) {
            val (width, height) = if (document.isFullscreen) {
                val jsScreen: Screen = jsWindow.screen
                if (currentWidth == jsScreen.width && currentHeight == jsScreen.height) return
                if (jsWindow.isMobileKeyboardVisible) return
                currentWidth = jsScreen.width
                currentHeight = jsScreen.height
                mainCanvas.setHtmlCanvasSize(currentWidth, currentHeight)
            } else {
                if (currentWidth == rootElement.clientWidth && currentHeight == rootElement.clientHeight) return
                if (jsWindow.isMobileKeyboardVisible) return
                currentWidth = rootElement.clientWidth
                currentHeight = rootElement.clientHeight
                mainCanvas.setHtmlCanvasSize(currentWidth, currentHeight - rootElement.titlebarHeight)
            }
            window.resize(width, height)
            window.updateLayout()
        } else {
            if (currentWidth == rootElement.clientWidth && currentHeight == rootElement.clientHeight) return
            log("[MainView.kt] clientHeight before resize: ${rootElement.clientHeight}")
            val (width, height) = mainCanvas.setHtmlCanvasSize(rootElement.clientWidth, rootElement.clientHeight)
            window.resize(width, height)
            window.updateLayout()
            currentWidth = rootElement.clientWidth
            currentHeight = rootElement.clientHeight
            log("[MainView.kt] clientHeight after resize: ${rootElement.clientHeight}")
        }
    }

}
