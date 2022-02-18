@file:JvmName("Instrumentation")

package featurea

import kotlin.system.exitProcess

actual val featureaDir: String get() = ""

actual fun exitProcess(status: Int): Nothing {
    exitProcess(status)
}

actual suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T = error("stub")
