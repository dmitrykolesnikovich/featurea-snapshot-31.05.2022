@file:JvmName("Instrumentation")

package featurea.utils

import featurea.jvm.distPath
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis
import java.lang.System as JvmSystem

actual val featureaDir: String by lazy {
    if (isTargetDistPathExeFile()) {
        emptyString
    } else {
        val featureaDir: String? = JvmSystem.getenv("featureaDir")
        if (featureaDir != null) {
            featureaDir
        } else {
            emptyString
        }
    }
}

actual fun exitProcess(status: Int): Nothing {
    exitProcess(status)
}

actual fun systemProperty(key: String): String? {
    return JvmSystem.getenv(key)
}

inline fun logTimeMillis(tag: String, block: () -> Unit) {
    val elapsedTime = measureTimeMillis(block)
    log("tag: ${elapsedTime}ms")
}

fun isTargetDistPathExeFile(): Boolean {
    return SystemTarget::class.java.distPath?.extension == "exe"
}
