package featurea.window

import featurea.layout.View
import featurea.runtime.Component

expect class WindowElement

interface WindowElementProvider<T : View> {
    fun Component.createElementOrNull(view: T): WindowElement?
    fun Component.destroyElement(element: WindowElement)
}
