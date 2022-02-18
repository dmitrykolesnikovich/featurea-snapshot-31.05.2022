package featurea.orientation

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.orientation") {
    includeExternals()
    include(featurea.window.artifact)

    "OrientationService" to ::OrientationService
    "OrientationServiceDelegate" to ::OrientationServiceDelegate
}
