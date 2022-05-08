package featurea.app

import featurea.runtime.*

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

@Constructor
fun Component.ApplicationController(update: () -> Unit) = object : ApplicationController(module) {
    override suspend fun update() = update()
}
