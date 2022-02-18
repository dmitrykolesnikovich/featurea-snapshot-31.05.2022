package featurea.cameraView

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.cameraView") {
    includeExternals()
    include(featurea.window.artifact)
}
