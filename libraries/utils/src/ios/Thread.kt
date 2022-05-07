package featurea.utils

import platform.Foundation.NSThread
import kotlin.native.concurrent.freeze

actual fun currentThreadSpecifier(): String = NSThread.currentThread.name ?: error("currentThreadSpecifier")

actual fun currentThread(): Any = NSThread.currentThread

actual fun <T> runBlocking(block: suspend () -> T): T {
    return runOnMainThread(block)
}

actual fun runOnApplicationThread(block: suspend () -> Unit) {
    runOnMainThread(block)
}

actual fun runOnEditorThread(block: suspend () -> Unit) {
    runOnMainThread(block)
}

/*internals*/

private fun <T> runOnMainThread(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking {
        block()
    }.freeze()
}
