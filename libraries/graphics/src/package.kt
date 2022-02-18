package featurea.graphics

import featurea.featureaDir
import featurea.runtime.Artifact

/*content*/

object Resources {
    val defaultShader = "featurea/graphics/shaders/default.shader"
}

/*dependencies*/

val artifact = Artifact("featurea.graphics") {
    includeContentRoot { "$featureaDir/engine/libraries/graphics/res" }
    include(featurea.shader.artifact)
    include(featurea.image.artifact)
    include(featurea.window.artifact)
}
