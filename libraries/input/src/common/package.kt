package featurea.input

import featurea.runtime.*

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.input") {
    includeExternals()
    include(featurea.window.artifact)

    "Input" to ::Input
}

fun DependencyBuilder.InputPlugin(plugin: Plugin<Input>) = install(plugin)

/*preferences*/

const val DEFAULT_DOUBLE_CLICK_DELAY: Double = 215.0
