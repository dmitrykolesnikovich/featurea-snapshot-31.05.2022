package featurea.opengl

import featurea.runtime.Component
import featurea.runtime.Provide
import featurea.runtime.provide

@Provide(OpenglProxy::class)
fun Component.provideOpenglProxy() {
    provide(OpenglProxy(OpenglImpl(module)))
}
