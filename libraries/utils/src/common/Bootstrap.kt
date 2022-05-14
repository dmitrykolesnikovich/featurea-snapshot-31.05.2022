package featurea.utils

import featurea.runtime.Component

// https://github.com/dmitrykolesnikovich/tetris/blob/main/src/main.kt#L34
object Bootstrap {

    var isSetup: Boolean = false
        private set

    fun export(components: List<() -> Component>): Bootstrap {
        check(!isSetup)
        // todo export components
        return this
    }

    fun export(component: () -> Component): Bootstrap {
        check(!isSetup)
        // todo export component
        return this
    }

    // `setup` instead of `run`
    fun setup() {
        check(!isSetup)
        isSetup = true
        // todo complete bootstrap
    }

}