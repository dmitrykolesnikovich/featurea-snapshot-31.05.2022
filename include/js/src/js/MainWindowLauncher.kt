package featurea.js

import featurea.js.dialog.appendMainCanvasDialog
import featurea.js.dialog.registerEscapeListener
import featurea.js.dialog.showDialogBox
import featurea.runtime.Module
import featurea.runtime.Runtime
import featurea.runtime.proxyScope
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import kotlin.collections.set

private val modules = mutableMapOf<String, Module>()
private val rootElements = mutableMapOf<String, HTMLElement>().also { registerEscapeListener(it) }

fun appendMainWindowOnLaunchWithArgs(onLaunch: (args: Array<String>) -> Runtime, args: Array<String>) {
    appendMainWindow(name = args[0], splashImagePath = args[1], screenRatio = args[2].toFloat()) {
        onLaunch(args)
    }
}

fun appendMainWindow(name: String, splashImagePath: String?, screenRatio: Float, mainCall: () -> Runtime) {
    val body: HTMLElement = document.body ?: error("document: $document")
    val dialogId: String = "$name-dialog"
    val dialog: HTMLElement? = document.getElementById(dialogId) as HTMLElement?
    if (dialog == null) {
        val rootElement: HTMLElement = body.appendMainCanvasDialog(name, screenRatio, splashImagePath)
        rootElements[name] = rootElement
        rootElement.id = dialogId
        proxyScope {
            onInitModule { module ->
                module.components.inject("_rootElement", rootElement)
                modules[name] = module
                showDialogBox(dialogId)
            }
            mainCall()
        }
    } else {
        dialog.style.display = "block"
    }
}

fun removeAllMainWindows() {
    for (name in ArrayList(rootElements.keys)) {
        removeMainWindow(name)
    }
}

fun removeMainWindow(name: String) {
    rootElements.remove(name)
    modules.remove(name)?.destroy()

    val body: HTMLElement = document.body ?: error("document: $document")
    val dialogId: String = "$name-dialog"
    val dialog: HTMLElement? = document.getElementById(dialogId) as HTMLElement?
    if (dialog != null) {
        body.removeChild(dialog)
    }

    js("if (window.updateProjects) window.updateProjects()") // quickfix todo improve
}
