package featurea.browser

import featurea.runtime.Artifact

val artifact = Artifact("featurea.browser") {
    include(featurea.app.artifact)

    "Browser" to ::Browser
}
