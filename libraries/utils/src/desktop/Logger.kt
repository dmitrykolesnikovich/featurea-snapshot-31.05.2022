@file:JvmName("LoggerUtils")

package featurea.utils

import featurea.utils.currentThread
import featurea.utils.currentThreadSpecifier
import java.io.PrintStream
import java.lang.System as JvmSystem

actual fun log(message: Any?, isFailure: Boolean) {
    // exception
    if (message is Throwable) {
        message.printStackTrace()
    }

    // text
    val text: String = if (message is Throwable) {
        message.localizedMessage
    } else {
        message.toString()
    }

    // print
    val printStream: PrintStream = if (isFailure) JvmSystem.err else JvmSystem.out
    printStream.println("${nowString()} [${currentThreadSpecifier()}@${currentThread().hashCode()}] - $text")
}
