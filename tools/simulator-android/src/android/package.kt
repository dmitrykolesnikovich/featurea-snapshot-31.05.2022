package featurea.android.simulator

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install

/*dependencies*/

val artifact = Artifact("featurea.android.simulator") {
    include(featurea.android.artifact)
    include(featurea.loader.artifact)
    include(featurea.orientation.artifact)
    include(featurea.settings.artifact)

    "BundleLauncher" to ::BundleLauncher
    "FileWatcherThread" to ::FileWatcherThread
    "Simulator" to ::Simulator
    "SimulatorContainer" to ::SimulatorContainer
    "SimulatorModule" to ::SimulatorModule

    SimulatorPlugin {
        "UploaderFeature" to ::UploaderFeature
    }
}

fun DependencyBuilder.SimulatorPlugin(plugin: Plugin<Simulator>) = install(plugin)

/*preferences*/

const val PREFERENCES_USER_EXISTS = "USER_EXISTS"
const val PREFERENCES_USERNAME = "USERNAME" // quickfix todo improve
const val PREFERENCES_PASSWORD = "PASSWORD" // quickfix todo improve
