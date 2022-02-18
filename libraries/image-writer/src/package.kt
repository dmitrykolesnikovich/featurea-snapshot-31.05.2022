package featurea.image.writer

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.image.writer") {
    include(featurea.image.reader.artifact)
    include(featurea.script.artifact)
    include(featurea.packTextures.artifact) // quickfix todo improve

    "ImageWriter" to ImageWriter::class

    static {
        provideComponent(ImageWriter(container = this))
    }
}
