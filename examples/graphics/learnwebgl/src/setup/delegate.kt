package featurea.examples.webgl

import featurea.*
import featurea.input.Input
import featurea.loader.Loader
import featurea.math.Matrix
import featurea.opengl.*
import featurea.runtime.import
import featurea.shader.ShaderContent
import featurea.window.Window

class Context : ApplicationContext() {

    val gl: Opengl = import(OpenglProxy)
    val input: Input = import()
    val loader: Loader = import()
    val shaderContent: ShaderContent = import()
    val window: Window = import()

    val projectionMatrix: Matrix = Matrix()

}
