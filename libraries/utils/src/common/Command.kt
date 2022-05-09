package featurea.utils

import kotlin.jvm.JvmName

data class CommandOptions(val name: String?, val workingDir: String? = null, val args: Map<String, String> = emptyMap())

class CommandNotFoundException(command: String) : RuntimeException(command)

suspend fun runCommand(command: String, name: String? = null, log: StringBlock = {}): Int {
    return runCommand(command, CommandOptions(name), timeout = 600_000L, log)
}

expect suspend fun runCommand(command: String, options: CommandOptions, timeout: Long, log: StringBlock): Int

expect suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T

@JvmName("parseCommandOptionsVarargs")
fun parseCommandOptions(vararg args: String, run: (command: String, options: Array<String>) -> Unit) {
    parseCommandOptions(args.toList().toTypedArray(), run)
}

fun parseCommandOptions(args: Array<String>, run: (command: String, options: Array<String>) -> Unit) {
    if (args.isEmpty()) error("args not found")
    val command = args[0]
    val options = if (args.size == 1) emptyArray() else args.copyOfRange(1, args.size)
    return run(command, options)
}
