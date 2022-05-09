package featurea.text.reader

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.text.reader") {
    include(featurea.content.artifact)

    "TextReader" to TextReader::class

    static {
        provideComponent(TextReader(container = this)) // todo refactor to `provideComponent(TextReader())` with Kotlin 1.6.2 feature "multiple context receivers"
    }
}
