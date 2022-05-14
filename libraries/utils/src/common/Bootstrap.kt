package featurea.utils

import featurea.runtime.Component

// https://github.com/dmitrykolesnikovich/tetris/blob/main/src/main.kt#L34
object Bootstrap {

    var isCompleted: Boolean = false
        private set

    fun export(components: List<() -> Component>): Bootstrap {
        check(!isCompleted)
        // todo export components
        return this
    }

    fun export(component: () -> Component): Bootstrap {
        check(!isCompleted)
        // todo export component
        return this
    }

    fun run() {
        check(!isCompleted)
        isCompleted = true
        // todo complete bootstrap
    }

}