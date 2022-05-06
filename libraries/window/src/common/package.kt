package featurea.window

import featurea.Application
import featurea.ApplicationDelegate
import featurea.bootstrapApplication
import featurea.runtime.*

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.window") {
    includeExternals()
    include(featurea.loader.artifact)

    "Window" to ::Window
    "WindowDelegate" to ::WindowDelegate
}

fun DependencyBuilder.WindowPlugin(plugin: Plugin<Window>) = install(plugin)

/*runtime*/

fun bootstrap(delegate: Component.() -> ApplicationDelegate) = bootstrapApplication(export = artifact) {
    val app: Application = import()
    app.delegate = app.delegate()
}
