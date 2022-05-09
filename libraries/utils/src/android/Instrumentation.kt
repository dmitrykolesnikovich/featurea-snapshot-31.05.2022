@file:JvmName("Instrumentation")

package featurea.utils

import kotlin.system.exitProcess
import java.lang.System as JvmSystem

actual val featureaDir: String get() = ""

actual fun exitProcess(status: Int): Nothing {
    exitProcess(status)
}

actual fun systemProperty(key: String): String? {
    return JvmSystem.getenv(key)
}
