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
import featurea.utils.featureaDir
import featurea.window.Window
import featurea.window.WindowListener
import featurea.window.notifyResize

/*artifact*/

val components = Artifact("featurea.examples.webgl") {
    includeContentRoot { "$featureaDir/engine/examples/webgl/res" }
    include(featurea.input.artifact)
    include(featurea.shader.artifact)

    "Context" to { _ -> Context() }
}

/*content*/

object Resources {
    val test1Shader = "test1.shader"
}

val bootstrapResources: List<String> = listOf(test1Shader)
