package featurea.app

import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

abstract class ApplicationController(override val module: Module) : Component {

    private val app: Application = import()

    var isEnable: Boolean = true

    override fun onCreateComponent() {
        app.controllers.add(this)
    }

    override fun onDeleteComponent() {
        app.controllers.remove(this)
    }

    abstract suspend fun update()

}

// constructor
fun Component.ApplicationController(update: () -> Unit) = object : ApplicationController(module) {
    override suspend fun update() = update()
}
