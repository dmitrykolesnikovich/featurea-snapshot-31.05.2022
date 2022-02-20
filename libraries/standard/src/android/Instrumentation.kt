@file:JvmName("Instrumentation")

package featurea

import kotlin.system.exitProcess
import java.lang.System as JvmSystem

actual val featureaDir: String get() = ""

actual fun exitProcess(status: Int): Nothing {
    exitProcess(status)
}

actual fun systemProperty(key: String): String? {
    return JvmSystem.getenv(key)
}

actual suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T = error("stub")
