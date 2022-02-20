@file:JvmName("CommandUtils")

package featurea

import featurea.jvm.existingFileOrNull
import featurea.jvm.isWindows
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.concurrent.thread
import kotlin.coroutines.suspendCoroutine

fun startProcess(command: String, options: Options? = null, name: String? = null, workingDir: String? = null): Process {
    if (!isInstrumentationEnabled) error("instrumentation not enabled")

    @Suppress("NAME_SHADOWING")
    val name: String = name ?: command.splitAndTrim(" ").first().splitAndTrim("/").last()

    // action
    val args: List<String> = command.trim().splitWithWrappers(' ')
    val args2 = ArrayList(args.map { it.removePrefix("\"").removeSuffix("\"").removePrefix("'").removeSuffix("'") })
    val tool: String = args2[0]
    val executableFileInWorkingDir: File? = existingFileOrNull(tool) ?: existingFileOrNull("${tool}.exe")
    val executableFile: File = if (executableFileInWorkingDir != null) {
        executableFileInWorkingDir
    } else {
        val toolPath: String = Tools[tool] ?: throw CommandNotFoundException(tool)
        val toolPathResolved: String = when {
            isWindows -> "${toolPath}.bat"
            else -> toolPath
        }
        File(toolPathResolved)
    }
    args2[0] = executableFile.absolutePath.correctPathDelimiter()
    val processBuilder: ProcessBuilder = ProcessBuilder(args2)
    if (options != null) {
        val environment: MutableMap<String, String> = processBuilder.environment()
        for ((key, value) in options) {
            environment[key] = value
        }
    }
    if (workingDir != null) {
        processBuilder.directory(File(workingDir))
    }
    println(args2.joinToString(separator = " "))
    if (options != null && options.entries.isNotEmpty()) {
        println("Options: ${options.entries.joinToString()}")
    }
    println(name)
    val process: Process = processBuilder.start()
    return process
}

@Suppress("NewApi")
actual suspend fun runCommand(
    command: String,
    name: String?,
    workingDir: String?,
    options: Options,
    timeout: Long,
    log: StringBlock
): Int {
    val process = startProcess(command, options, name, workingDir)
    if (timeout == -1L) {
        return 0
    } else {
        return suspendCoroutine { continuation ->
            thread {
                process.inputStream.reader().forEachLine { line ->
                    log(line)
                }
                if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                    process.destroy()
                    val exception = RuntimeException("execution timed out: $command")
                    continuation.resumeWith(Result.failure(exception))
                }
                continuation.resumeWith(Result.success(process.exitValue()))
            }
        }
    }
}

fun startProcessWithLogThreads(
    command: String,
    name: String? = null,
    options: Options? = null,
    workingDir: String? = null,
    log: StringBlock = {}
): Process {
    val process = startProcess(command, options, name, workingDir)
    thread {
        process.inputStream.reader().forEachLine { line ->
            log(line)
        }
    }
    thread {
        process.errorStream.reader().forEachLine { line ->
            log(line)
        }
    }
    return process
}
