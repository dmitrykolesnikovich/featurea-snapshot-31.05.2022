package featurea.app

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install

/*dependencies*/

val artifact = Artifact("featurea.app") {
    include(featurea.utils.artifact)

    "Application" to ::Application
    "ApplicationContainer" to ::ApplicationContainer
    "ApplicationModule" to ::ApplicationModule
}

fun DependencyBuilder.ApplicationPlugin(plugin: Plugin<Application>) = install(plugin)
