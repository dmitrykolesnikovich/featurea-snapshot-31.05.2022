package featurea.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val currentThread: String = "main"

actual fun currentThreadSpecifier(): String = "main"

actual fun currentThread(): Any = currentThread

actual fun <T> runBlocking(block: suspend () -> T): T = error("stub")

actual fun runOnApplicationThread(block: suspend () -> Unit) = runOnMainThread(block)

actual fun runOnEditorThread(block: suspend () -> Unit) = runOnMainThread(block)

private fun runOnMainThread(block: suspend () -> Unit) {
    GlobalScope.launch {
        block()
    }
}
