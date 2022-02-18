package featurea.shader.writer

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.shader.writer") {
    include(featurea.shader.reader.artifact)

    "ShaderWriter" to ShaderWriter::class

    static {
        provideComponent(ShaderWriter(container = this))
    }
}
