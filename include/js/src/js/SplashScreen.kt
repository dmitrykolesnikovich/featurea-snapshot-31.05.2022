package featurea.js

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.js.onLoadFunction
import kotlinx.html.style
import org.w3c.dom.HTMLElement

fun loadSplashScreen(projectName: String, splashImagePath: String?, complete: () -> Unit) {
    // setup
    val splashId = "$projectName-splash"

    // filter
    val body: HTMLElement = document.body ?: error("document: $document")
    if (splashImagePath == null || splashImagePath.isBlank()) {
        complete()
        return
    }
    if (document.getElementById(splashId) != null) {
        complete()
        return
    }

    // action
    body.append {
        img {
            id = splashId
            src = splashImagePath
            style = "display: none;"
            onLoadFunction = { println("[SUCCESS] $splashImagePath is loaded"); complete() }
        }
    }
}
