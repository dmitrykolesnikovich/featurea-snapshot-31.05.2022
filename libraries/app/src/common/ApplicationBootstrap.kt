package featurea.app

import featurea.runtime.Action
import featurea.runtime.Component
import featurea.runtime.Dependency
import featurea.runtime.Runtime

// https://github.com/dmitrykolesnikovich/tetris/blob/main/src/main.kt#L19
object ApplicationBootstrap {

    private var isRunning: Boolean = false

    fun export(components: List<() -> Component>): ApplicationBootstrap {
        check(!isRunning)
        // todo export components
        return this
    }

    fun export(component: () -> Component): ApplicationBootstrap {
        check(!isRunning)
        // todo export component
        return this
    }

    fun load(resources: List<String>): ApplicationBootstrap {
        check(!isRunning)
        // todo load resources
        return this
    }

    fun <T> run(complete: T.() -> Unit) {
        check(!isRunning)
        isRunning = true
        // todo complete bootstrap
    }

}

fun bootstrapApplication(export: Dependency, setup: Action = {}): Runtime = Runtime {
    exportComponents(export)
    injectContainer("featurea.app.ApplicationContainer")
    injectModule("featurea.app.ApplicationModule")
    complete(setup)
}
