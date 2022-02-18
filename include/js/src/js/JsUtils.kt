package featurea.js

import kotlinx.html.INPUT
import kotlinx.html.TagConsumer
import kotlinx.html.attributesMapOf
import kotlinx.html.dom.append
import kotlinx.html.visitAndFinalize
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

val Document.isFullscreen: Boolean get() = fullscreenElement != null

fun HTMLElement.createInputElement(init: INPUT.() -> Unit = {}): HTMLInputElement {
    return append.inputElement(init)
}

/*internals*/

private fun <T, C : TagConsumer<T>> C.inputElement(block: INPUT.() -> Unit): HTMLInputElement {
    return INPUT(attributesMapOf(), this).visitAndFinalize(this, block) as HTMLInputElement
}
