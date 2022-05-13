package featurea.app

import featurea.runtime.Action
import featurea.runtime.Dependency
import featurea.runtime.Runtime

// https://github.com/dmitrykolesnikovich/tetris
class ApplicationBootstrap internal constructor() {
    fun export(components: Dependency): ApplicationBootstrap = TODO("Not implemented yet")
    fun load(resources: List<String>): ApplicationBootstrap = TODO("Not implemented yet")
    fun <T> run(complete: T.() -> Unit): Unit = TODO("Not implemented yet")
}

fun bootstrapApplication(export: Dependency, setup: Action = {}): Runtime = Runtime {
    exportComponents(export)
    injectContainer("featurea.app.ApplicationContainer")
    injectModule("featurea.app.ApplicationModule")
    complete(setup)
}
