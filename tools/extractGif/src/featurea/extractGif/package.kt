package featurea.extractGif

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.gifExtractor") {
    "GifExtractor" to GifExtractor::class

    static {
        provideComponent(GifExtractor())
    }
}
