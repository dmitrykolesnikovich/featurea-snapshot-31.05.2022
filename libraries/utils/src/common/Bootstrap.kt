package featurea.utils

import featurea.runtime.Component

// https://github.com/dmitrykolesnikovich/tetris/blob/main/src/main.kt#L34
object Bootstrap {

    var isRunning: Boolean = false
        private set

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

    fun run() {
        check(!isRunning)
        isRunning = true
        // todo complete bootstrap
    }

}