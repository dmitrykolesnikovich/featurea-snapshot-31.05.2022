package featurea

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install

/*dependencies*/

val artifact = Artifact("featurea") {
    include(featurea.utils.artifact)

    "Application" to ::Application
    "ApplicationContainer" to ::ApplicationContainer
    "ApplicationModule" to ::ApplicationModule
}

fun DependencyBuilder.ApplicationPlugin(plugin: Plugin<Application>) = install(plugin)
