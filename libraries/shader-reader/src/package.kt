package featurea.shader.reader

import featurea.runtime.Artifact

val artifact = Artifact("featurea.shader.reader") {
    include(featurea.content.artifact)

    "ShaderReader" to ShaderReader::class

    static {
        provideComponent(ShaderReader(staticModule))
    }
}
