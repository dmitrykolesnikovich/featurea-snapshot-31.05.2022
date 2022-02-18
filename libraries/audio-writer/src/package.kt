package featurea.audio.writer

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.audio.writer") {
    include(featurea.audio.reader.artifact)

    "AudioWriter" to AudioWriter::class
    
    static {
        provideComponent(AudioWriter(container = this))
    }
}
