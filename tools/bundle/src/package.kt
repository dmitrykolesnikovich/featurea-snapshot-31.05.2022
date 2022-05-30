package featurea.bundler

import featurea.bundler.Bundler
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.bundler") {
    include(featurea.rml.writer.artifact)

    "Bundler" to Bundler::class

    static {
        provideComponent(Bundler(staticModule))
    }
}
