package featurea.fullscreen

import featurea.js.HTMLCanvasElementProxy
import featurea.js.applyCssAttribute
import featurea.utils.parent
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.window.Window
import featurea.window.WindowElement
import featurea.window.WindowElementProvider
import featurea.window.provideWindowElement
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.EventListener

// https://www.w3schools.com/howto/howto_js_fullscreen.asp
fun FullScreenButtonProvider(module: Module) =
    module.provideWindowElement(object : WindowElementProvider<FullScreenButton> {

        private val window: Window = module.importComponent()
        private val mainCanvas: HTMLCanvasElement = module.importComponent(HTMLCanvasElementProxy)

        override fun Component.createElementOrNull(view: FullScreenButton): WindowElement {
            val div: HTMLDivElement = document.createElement("div") as HTMLDivElement
            mainCanvas.addEventListener("fullscreenchange", EventListener {
                view.isFullScreen = document.fullscreenElement != null
                window.updateLayout()
                div.updateBackgroundImage(view)
                view.onChangeFullScreen?.invoke()
            })
            div.updateBackgroundImage(view)
            div.onclick = {
                js(
                    """
                if (mainCanvas.requestFullscreen) {
                    mainCanvas.requestFullscreen();
                } else if (mainCanvas.mozRequestFullScreen) { /* Firefox */
                    mainCanvas.mozRequestFullScreen();
                } else if (mainCanvas.webkitRequestFullscreen) { /* Chrome, Safari and Opera */
                    mainCanvas.webkitRequestFullscreen();
                } else if (mainCanvas.msRequestFullscreen) { /* IE/Edge */
                    mainCanvas.msRequestFullscreen();
                }                    
                """
                )
            }
            return WindowElement(div)
        }

        override fun Component.destroyElement(element: WindowElement) {
            // no op
        }

    })

/*internals*/

private fun HTMLDivElement.updateBackgroundImage(view: FullScreenButton) {
    if (view.isFullScreen) {
        style.background = ""
    } else {
        style.backgroundSize = "contain"
        val contentRoot: String = window.location.href.parent
        style.applyCssAttribute("background-image" to "url('${contentRoot}/FullScreenButton.png')")
    }
}
