package featurea.utils

actual suspend fun runCommand(command: String, options: CommandOptions, timeout: Long, log: StringBlock): Int = error("stub")

actual suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T = error("stub")
