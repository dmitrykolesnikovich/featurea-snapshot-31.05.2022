package featurea.window

import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.window") {
    includeExternals()
    include(featurea.loader.artifact)

    "Window" to ::Window
    "WindowDelegate" to ::WindowDelegate
}

fun DependencyBuilder.WindowPlugin(plugin: Plugin<Window>) = install(plugin)
