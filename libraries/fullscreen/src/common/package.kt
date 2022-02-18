package featurea.fullscreen

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.fullscreen") {
    includeExternals()
    include(featurea.window.artifact)
}
