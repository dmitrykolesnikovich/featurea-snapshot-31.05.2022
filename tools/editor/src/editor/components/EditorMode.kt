package featurea.studio.editor.components

import featurea.Application
import featurea.studio.runOnUpdateOnJfxThread
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.coroutines.runBlocking

sealed class EditorMode {

    abstract operator fun invoke(block: suspend () -> Unit)

    object Edit : EditorMode() {
        override fun invoke(block: suspend () -> Unit) = runBlocking {
            block()
        }
    }

    class Play(override val module: Module) : Component, EditorMode() {

        private val app: Application = import()

        override fun invoke(block: suspend () -> Unit): Unit = app.runOnUpdateOnJfxThread {
            block()
        }

    }

}
