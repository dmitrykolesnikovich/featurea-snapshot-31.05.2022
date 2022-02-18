package featurea.dialog

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.dialog") {
    include(featurea.window.artifact)

    "AlertService" to ::AlertService
}
