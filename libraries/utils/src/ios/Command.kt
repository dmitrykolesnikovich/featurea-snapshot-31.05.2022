package featurea.utils

import featurea.utils.Options
import featurea.utils.StringBlock

actual suspend fun runCommand(
    command: String,
    name: String?,
    workingDir: String?,
    options: Options,
    timeout: Long,
    log: StringBlock
): Int = error("stub")