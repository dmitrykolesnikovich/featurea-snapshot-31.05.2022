package featurea.examples.learnopengl

import featurea.examples.learnopengl.Resources.imageShader
import featurea.examples.learnopengl.Resources.lightShader
import featurea.examples.learnopengl.Resources.objectShader
import featurea.examples.learnopengl.Resources.test3dShader
import featurea.examples.learnopengl.Resources.triangleShader

/*artifact*/

val components = Artifact("learnopengl") {
    includeContentRootWithDefaultConfig { "$featureaDir/engine/examples/learnopengl/res" }
    include(featurea.input.artifact) // required to work with graphics on android todo improve
    include(featurea.graphics.artifact)
    include(featurea.keyboard.artifact)
    include(featurea.orientation.artifact)

    "Context" to { module: Module -> module.applicationScope { Context() } }
    "TestDocket" to { module: Module -> module.applicationScope { TestDocket() } }
}

/*content*/

object Resources {
    val containerPng = "images/container.png"
    val containerSpecularPng = "images/containerSpecular.png"
    val imageShader = "shaders/image.shader"
    val smilePng = "images/smile.png"
    val testLinesShader = "shaders/testLines.shader"
    val test3dShader = "shaders/test3d.shader"
    val triangleShader = "shaders/triangle.shader"
    val lightShader = "shaders/Light.shader"
    val objectShader = "shaders/Object.shader"
}

val bootstrapResources: List<String> = listOf(
    triangleShader,
    imageShader,
    test3dShader,
    lightShader,
    objectShader,
)

/*runtime*/

fun bootstrapTest(setup: Context.() -> Unit): Runtime = bootstrapApplication(export = featurea.examples.learnopengl.components) {
    val app: Application = import()
    val context: Context = import()
    val loader: Loader = import()
    loader.loadResources(bootstrapResources) {
        context.setup()
        app.delegate = context
    }
}

fun bootstrapTestLines(setup: Context.() -> Unit): Runtime = bootstrapApplication(export = featurea.examples.learnopengl.components) {

}
