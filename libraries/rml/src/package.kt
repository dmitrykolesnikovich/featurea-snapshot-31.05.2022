package featurea.rml

import featurea.content.contentTypes
import featurea.runtime.Artifact

val artifact = Artifact("featurea.rml") {
    include(featurea.rml.reader.artifact)
    include(featurea.script.artifact)

    contentTypes {
        "RmlContentType" to ::RmlContentType
    }
}
