package featurea.opengl

import featurea.js.HTMLCanvasElementProxy
import featurea.js.dynamicMapOf
import featurea.runtime.Component
import featurea.runtime.Provide
import featurea.runtime.import
import featurea.runtime.provide
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement

// https://stackoverflow.com/a/39354174/909169
@Provide(OpenglProxy::class)
fun Component.provideOpenglProxy() {
    val mainCanvas: HTMLCanvasElement = import(HTMLCanvasElementProxy)
    val gl: OpenglImpl = OpenglImpl(module)
    gl.context = mainCanvas.getContext("webgl", dynamicMapOf("alpha" to false)) as WebGLRenderingContext
    provide(OpenglProxy(gl))
}
