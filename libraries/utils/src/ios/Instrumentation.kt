package featurea.utils

actual val featureaDir: String get() = ""

actual fun exitProcess(status: Int): Nothing = error("stub")

actual fun systemProperty(key: String): String? = ""
