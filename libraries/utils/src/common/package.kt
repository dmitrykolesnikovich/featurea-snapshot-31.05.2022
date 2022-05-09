package featurea.utils

import featurea.System
import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.utils") {
    includeExternals()

    "Device" to ::Device
    "System" to System::class

    static {
        provideComponent(System())
    }
}
