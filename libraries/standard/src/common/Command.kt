package featurea

import featurea.utils.StringBlock
import kotlin.jvm.JvmName

typealias Options = Map<String, String>

class CommandNotFoundException(command: String) : RuntimeException(command)

suspend fun launchCommand(command: String, options: Options = emptyMap(), workingDir: String?, log: StringBlock): Int {
    return runCommand(command, command, workingDir, options, -1, log)
}

suspend fun runCommand(command: String, name: String? = null, log: StringBlock = {}): Int {
    return runCommand(command, name, workingDir = null, options = emptyMap(), timeout = 600_000L, log)
}

expect suspend fun runCommand(command: String, name: String? = null, workingDir: String?, options: Options, timeout: Long, log: StringBlock): Int

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
