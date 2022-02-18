package featurea.examples.webgl

import featurea.*
import featurea.examples.webgl.Resources.test1Shader
import featurea.input.Input
import featurea.loader.Loader
import featurea.math.Matrix
import featurea.math.Size
import featurea.opengl.*
import featurea.runtime.Artifact
import featurea.runtime.import
import featurea.shader.ShaderContent
import featurea.window.Window
import featurea.window.WindowListener
import featurea.window.notifyResize

fun bootstrapTest(setup: Context.() -> Unit) = bootstrapApplication(export = featurea.examples.webgl.components) {
    val context: Context = applicationScope { import() }
    with(context) {
        window.size = Size(320, 480)
        window.title = "Draw Lines"
        // projectionMatrix
        window.listeners.add(object : WindowListener {
            override fun init() {
                log("[bootstrapTest] init: ${gl.getString(SHADING_LANGUAGE_VERSION)}")
                gl.enable(BLEND)
                gl.blendFunction(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
            }

            override fun resize(width: Int, height: Int) {
                gl.viewport(0, 0, width, height)
                window.surface.matrix.assignOrtho(0, 0, width, height)
                window.surface.viewport.assign(width, height)
                projectionMatrix.assign(window.surface.matrix).translate(window.surface.origin)
            }
        })

        // load
        isStandardShaderLibraryIncluded = false
        loader.loadResources(bootstrapResources) {
            setup()
            window.notifyResize()
        }
    }
}
