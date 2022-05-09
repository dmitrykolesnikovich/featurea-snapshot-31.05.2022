package featurea.window

import featurea.js.HtmlElementProxy
import featurea.js.setPosition
import featurea.js.setSize
import featurea.layout.View
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import kotlin.reflect.KClass

actual class WindowElement(val htmlElement: HTMLElement)

actual class WindowDelegate actual constructor(override val module: Module) : Component {

    private val htmlElement: HTMLElement by lazy { import(HtmlElementProxy) }
    private val window: Window = import()

    actual fun appendView(view: View) {
        val element: WindowElement = findWindowElement(view) ?: return
        val (x1, y1, x2, y2) = view.rectangle
        val ox: Int = htmlElement.getBoundingClientRect().left.toInt()
        val oy: Int = htmlElement.getBoundingClientRect().top.toInt()
        val x: Int = ox + x1.toInt()
        val y: Int = oy + y1.toInt()
        val width: Int = (x2 - x1).toInt()
        val height: Int = (y2 - y1).toInt()
        element.htmlElement.setPosition(x, y)
        element.htmlElement.setSize(width, height)
        element.htmlElement.style.position = "absolute"
        element.htmlElement.style.zIndex = "2"
        document.body?.appendChild(element.htmlElement)
    }

    actual fun removeView(view: View) {
        val element: WindowElement = window.elements.remove(view) ?: return
        val viewType: KClass<out View> = view::class
        val elementProvider: WindowElementProvider<View> = window.elementProviders[viewType] ?: return
        with(elementProvider) {
            destroyElement(element)
        }
        element.htmlElement.style.zIndex = "0"
        element.htmlElement.style.display = "none"
        document.body?.removeChild(element.htmlElement)
    }

    /*internals*/

    private fun findWindowElement(view: View): WindowElement? {
        // 1. existing
        val existingElement: WindowElement? = window.elements[view]
        if (existingElement != null) {
            return existingElement
        }

        // 2. newly created
        val viewType: KClass<out View> = view::class
        val elementProvider: WindowElementProvider<View> = window.elementProviders[viewType] ?: return null
        val element: WindowElement = with(elementProvider) { createElementOrNull(view) } ?: return null
        window.elements[view] = element
        return element
    }

}
