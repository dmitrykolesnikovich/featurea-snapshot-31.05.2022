package featurea.shader

import featurea.content.contentTypes
import featurea.featureaDir
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.shader") {
    include(featurea.opengl.artifact)
    include(featurea.shader.reader.artifact)

    "ShaderContent" to ::ShaderContent

    contentTypes {
        "ShaderContentType" to ::ShaderContentType
    }
}
