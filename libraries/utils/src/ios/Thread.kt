package featurea.utils

import platform.Foundation.NSThread
import kotlin.native.concurrent.freeze

actual fun currentThreadSpecifier(): String = NSThread.currentThread.name ?: error("currentThread")

actual fun currentThread(): Any = NSThread.currentThread

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

private fun runOnMainThread(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking {
        block()
    }.freeze()
}
