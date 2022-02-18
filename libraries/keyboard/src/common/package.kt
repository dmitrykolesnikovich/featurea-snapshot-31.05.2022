package featurea.keyboard

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.keyboard") {
    includeExternals()
    include(featurea.settings.artifact)
    include(featurea.window.artifact)

    "Keyboard" to ::Keyboard
    "KeyboardDelegate" to ::KeyboardDelegate

    KeyboardPlugin {
        "KeyEventProducer" to ::KeyEventProducer
    }
}

fun DependencyBuilder.KeyboardPlugin(plugin: Plugin<Keyboard>) = install(plugin)
