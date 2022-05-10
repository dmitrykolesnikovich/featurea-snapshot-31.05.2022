package featurea.rml.writer

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.rml.writer") {
    include(featurea.rml.artifact)

    "RmlDeserializer" to RmlDeserializer::class
    "RmlWriter" to RmlWriter::class

    static {
        provideComponent(RmlDeserializer(staticModule))
        provideComponent(RmlWriter(staticModule))
    }
}
