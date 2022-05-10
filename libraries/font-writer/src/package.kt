package featurea.font.writer

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.font.writer") {
    include(featurea.font.reader.artifact)
    include(featurea.content.artifact)

    "FontWriter" to FontWriter::class

    static {
        provideComponent(FontWriter(staticModule))
    }
}
