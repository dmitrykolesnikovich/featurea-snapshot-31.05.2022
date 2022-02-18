package featurea.text.reader

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.text.reader") {
    include(featurea.content.artifact)

    "TextReader" to TextReader::class

    static {
        provideComponent(TextReader(container = this))
    }
}
