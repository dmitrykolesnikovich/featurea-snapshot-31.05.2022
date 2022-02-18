package featurea

actual val featureaDir: String get() = ""

actual fun exitProcess(status: Int): Nothing = error("stub")

actual suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T = error("stub")
