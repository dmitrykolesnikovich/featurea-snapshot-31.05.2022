@file:Suppress("UNCHECKED_CAST")

package featurea.window

import featurea.layout.View
import featurea.runtime.Component
import featurea.runtime.Module
import kotlin.reflect.KClass

expect class WindowDelegate(module: Module) : Component {
    fun appendView(view: View)
    fun removeView(view: View)
}

inline fun <reified T : View> Module.provideWindowElement(windowElementProvider: WindowElementProvider<T>) =
    provideWindowElement(T::class, windowElementProvider)

fun <T : View> Module.provideWindowElement(viewType: KClass<T>, windowElementProvider: WindowElementProvider<T>) {
    val window: Window = importComponent()
    window.elementProviders[viewType] = windowElementProvider as WindowElementProvider<View>
}
