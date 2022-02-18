package featurea.browser

import featurea.runtime.Artifact

val artifact = Artifact("featurea.browser") {
    include(featurea.artifact)

    "Browser" to ::Browser
}
