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

class Context : ApplicationDelegateComponent() {

    val gl: Opengl = import(OpenglProxy)
    val input: Input = import()
    val loader: Loader = import()
    val shaderContent: ShaderContent = import()
    val window: Window = import()

    val projectionMatrix: Matrix = Matrix()

}
