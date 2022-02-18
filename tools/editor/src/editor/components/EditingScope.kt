package featurea.studio.editor.components

import featurea.runOnUpdateOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.coroutines.runBlocking

sealed class EditingScope {

    abstract fun launch(block: suspend () -> Unit)

    object Document : EditingScope() {
        override fun launch(block: suspend () -> Unit) = runBlocking {
            block()
        }
    }

    class Application(override val module: Module) : Component, EditingScope() {

        private val app: featurea.Application = import()

        override fun launch(block: suspend () -> Unit): Unit = app.runOnUpdateOnJfxThread {
            block()
        }

    }

}
