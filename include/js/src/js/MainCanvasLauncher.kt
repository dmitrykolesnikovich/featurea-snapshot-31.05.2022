package featurea.js

import featurea.runtime.Runtime
import featurea.runtime.proxyScope
import kotlinx.browser.document
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.style
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.collections.set

private val canvasDivs = mutableMapOf<String, HTMLElement>()

fun appendMainCanvasOnLaunchWithArgs(onLaunch: (args: Array<String>) -> Runtime, args: Array<String>) {
    appendMainCanvas(name = args[0], rootSelector = args[7]) {
        onLaunch(args)
    }
}

/*internals*/

private fun appendMainCanvas(name: String, rootSelector: String, init: () -> Runtime) {
    val canvasRoot: Element = document.querySelector(rootSelector) ?: error("document: $document")
    canvasRoot as HTMLElement
    val canvasDivId: String = "$name-canvas"
    if (document.getElementById(canvasDivId) == null) {
        val canvasDiv: HTMLElement = canvasRoot.append {
            div {
                canvas {
                    id = "mainCanvas"
                    style = "display: hidden; background-color: white; background: white"
                }
            }
        }.first()
        canvasDivs[name] = canvasDiv
        canvasDiv.id = canvasDivId
        canvasDiv.style.height = canvasRoot.style.height
        proxyScope {
            onInitModule { module ->
                module.components.inject("_rootElement", canvasDiv)
                module.components.inject("useMainCanvas", true)

                // >> quickfix todo simplify
                val externals = mutableMapOf<String, Any>()
                module.components.inject("externals", externals)
                export("findExternal") { key: String ->
                    externals[key]
                }
                // <<
            }
            init()
        }
    }
}
