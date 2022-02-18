package featurea.loader

import featurea.ApplicationPlugin
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.loader") {
    include(featurea.rml.artifact)
    include(featurea.script.artifact)

    "Loader" to ::Loader
    "LoaderController" to ::LoaderController

    /*ApplicationPlugin {

    }*/
}
