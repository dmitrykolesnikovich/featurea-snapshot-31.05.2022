package featurea.utils

import featurea.runtime.Component

// https://github.com/dmitrykolesnikovich/tetris/blob/main/src/main.kt#L34
object Bootstrap {

    private var isRunning: Boolean = false

    fun export(components: List<() -> Component>): Bootstrap {
        check(!isRunning)
        // todo export components
        return this
    }

    fun export(component: () -> Component): Bootstrap {
        check(!isRunning)
        // todo export component
        return this
    }

    fun load(resources: List<String>): Bootstrap {
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