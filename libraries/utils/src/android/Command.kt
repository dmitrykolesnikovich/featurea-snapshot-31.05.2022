@file:JvmName("CommandUtils")
package featurea.utils

actual suspend fun runCommand(
    command: String,
    name: String?,
    workingDir: String?,
    options: Options,
    timeout: Long,
    log: StringBlock
): Int = error("stub")