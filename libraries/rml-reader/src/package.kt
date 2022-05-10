package featurea.rml.reader

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.rml.reader") {
    include(featurea.text.artifact)

    "RmlContent" to RmlContent::class
    "RmlReader" to RmlReader::class

    static {
        provideComponent(RmlContent(staticModule))
        provideComponent(RmlReader(staticModule))
    }
}
