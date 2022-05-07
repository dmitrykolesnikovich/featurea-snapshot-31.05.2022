package featurea.examples.drawLines

import featurea.ApplicationComponent
import featurea.applicationScope
import featurea.bootstrapApplication
import featurea.examples.drawLines.Resources.drawLineShader
import featurea.examples.drawLines.Resources.drawRectangleShader
import featurea.examples.drawLines.Resources.tutorialShader
import featurea.utils.featureaDir
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

/*artifact*/

val components = Artifact("featurea.examples.drawLines") {
    includeContentRoot { "$featureaDir/engine/examples/drawLines/res" }
    include(featurea.input.artifact)
    include(featurea.shader.artifact)

    "Context" to { _ -> Context() }
}

class Context : ApplicationComponent() {

    val gl: Opengl = import(OpenglProxy)
    val input: Input = import()
    val loader: Loader = import()
    val shaderContent: ShaderContent = import()
    val window: Window = import()

    val projectionMatrix: Matrix = Matrix()

}

/*content*/

object Resources {
    val tutorialShader = "tutorial.shader"
    val drawLineShader = "drawLine.shader"
    val drawRectangleShader = "drawRectangle.shader"
}

val bootstrapResources: List<String> = listOf(tutorialShader, drawLineShader, drawRectangleShader)

/*runtime*/

fun bootstrapTest(setup: Context.() -> Unit) = bootstrapApplication(export = featurea.examples.drawLines.components) {
    val context: Context = applicationScope { import() }
    with(context) {
        window.size = Size(320, 480)
        window.title = "Draw Lines"
        // projectionMatrix
        window.listeners.add(object : WindowListener {
            override fun init() {
                gl.enable(BLEND)
                gl.blendFunction(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
                /*gl.blendEquationSeparate(FUNC_ADD, FUNC_ADD)
                gl.blendFunctionSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)*/
            }

            override fun resize(width: Int, height: Int) {
                gl.viewport(0, 0, width, height)
                window.surface.matrix.assignOrtho(0, 0, width, height)
                window.surface.viewport.assign(width, height)
                projectionMatrix.assign(window.surface.matrix).translate(window.surface.origin)
            }
        })

        // load
        loader.loadResources(bootstrapResources) {
            setup()
            window.notifyResize()
        }
    }
}
