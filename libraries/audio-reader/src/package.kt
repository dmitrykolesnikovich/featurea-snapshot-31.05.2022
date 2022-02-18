package featurea.audio.reader

import featurea.runtime.Artifact

val artifact = Artifact("featurea.audio.reader") {
    include(featurea.content.artifact)

    "AudioReader" to AudioReader::class

    static {
        provideComponent(AudioReader())
    }
}
