package featurea.js

import featurea.runtime.Runtime
import featurea.runtime.buildRuntime
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

fun loadRootElementAsBody(mainCall: () -> Runtime) {
    window.loadBody {
        buildRuntime {
            onInitModule { module ->
                val body: HTMLElement = window.document.body ?: error("document: ${window.document}")
                module.components.inject("_rootElement", body)
            }
            mainCall()
        }
    }
}

fun loadRootElementAsDialog(mainCall: () -> Runtime) {
    window.loadBody {
        appendMainWindow(document.title, splashImagePath = null, 1f, mainCall)
    }
}
