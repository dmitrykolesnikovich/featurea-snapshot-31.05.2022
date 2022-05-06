package featurea.js

import featurea.Application
import featurea.utils.parent
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.events.Event
import kotlinx.browser.window as jsWindow
import org.w3c.dom.events.EventListener as JsEventListener

class ProgressView(override val module: Module) : Component, JsEventListener {

    private val app: Application = import()
    private val rootElement: HTMLElement = import(RootElementProxy)
    private val splashImage: HTMLImageElement by lazy { import(SplashImageProxy) }
    private val titlebar: HTMLElement by lazy { import(TitlebarProxy) }
    private val closeButton: HTMLElement by lazy { import(CloseButtonProxy) }
    private val mainCanvas: HTMLCanvasElement by lazy { import(HTMLCanvasElementProxy) }

    private val isStandalone: Boolean = rootElement.querySelector(".titlebar") == null // quickfix todo improve
    private val hasSplash by lazy { splashImage.src != "${jsWindow.location.href.parent}/_" }
    private val loadingImage: HTMLImageElement by lazy { rootElement.querySelector("#loadingImage") as HTMLImageElement }
    private val splashDiv: HTMLDivElement by lazy { loadingImage.parentElement as HTMLDivElement }

    private lateinit var touch: () -> Unit
    private lateinit var finish: () -> Unit

    init {
        when {
            isStandalone -> finish = { mainCanvas.style.display = "inline" } // quickfix todo improve
            hasSplash -> initSplashScreen()
            else -> initLoadingScreen()
        }
        app.runOnCompleteLoading {
            finish()
        }
    }

    override fun handleEvent(event: Event) {
        if (isStandalone) return // quickfix todo improve

        event.preventDefault()
        when (event.type) {
            "mousedown", "touchstart" -> touch()
        }
    }

    /*internals*/

    private fun initSplashScreen() {
        titlebar.style.visibility = "hidden"
        closeButton.style.visibility = "hidden"
        splashImage.style.display = "inline"
        loadingImage.style.display = "none"
        mainCanvas.style.display = "none"
        rootElement.style.borderColor = ""
        splashDiv.style.backgroundColor = ""
        splashDiv.style.alignItems = ""

        registerInputListener { initLoadingScreen() }
        finish = { registerInputListener { initMainScreen() } }
    }

    private fun initLoadingScreen() {
        titlebar.style.visibility = "hidden"
        closeButton.style.visibility = "hidden"
        splashImage.style.display = "none"
        loadingImage.style.display = "inline"
        mainCanvas.style.display = "none"
        rootElement.style.borderColor = "black"
        splashDiv.style.backgroundColor = "white"
        splashDiv.style.alignItems = "center"

        unregisterListener()
        finish = { initMainScreen() }
    }

    private fun initMainScreen() {
        titlebar.style.visibility = "visible"
        closeButton.style.visibility = "visible"
        splashImage.style.display = "none"
        loadingImage.style.display = "none"
        mainCanvas.style.display = "inline"
        rootElement.style.borderColor = ""
        splashDiv.style.backgroundColor = ""
        splashDiv.style.alignItems = ""

        unregisterListener()
    }

    private fun registerInputListener(touch: () -> Unit) {
        this.touch = touch
        rootElement.addEventListener("mousedown", this, false)
        rootElement.addEventListener("touchstart", this, false)
    }

    private fun unregisterListener() {
        rootElement.removeEventListener("mousedown", this)
        rootElement.removeEventListener("touchstart", this)
    }

}
