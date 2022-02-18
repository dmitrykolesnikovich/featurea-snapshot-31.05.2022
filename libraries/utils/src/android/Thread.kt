@file:JvmName("ThreadUtils")

package featurea.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual fun currentThreadSpecifier(): String = Thread.currentThread().toString()

actual fun currentThread(): Any = Thread.currentThread()

actual fun <T> runBlocking(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking {
        block()
    }
}

actual fun runOnApplicationThread(block: suspend () -> Unit) {
    runOnMainThread(block)
}

actual fun runOnEditorThread(block: suspend () -> Unit) {
    runOnMainThread(block)
}

/*internals*/

private fun runOnMainThread(block: suspend () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        block()
    }
}
