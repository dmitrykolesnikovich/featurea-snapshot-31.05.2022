package featurea.utils

import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlinx.browser.window as jsWindow

actual val featureaDir: String get() = ""

actual fun exitProcess(status: Int): Nothing = error("stub")

actual fun systemProperty(key: String): String? = ""

actual suspend fun <T> executeAsyncJsAction(action: String, vararg args: String): T {
    val argsStartIndex = action.indexOf("(")
    val functionPath = action.substring(0, argsStartIndex)
    val asyncJsFunction = findFunction(functionPath)

    return suspendCoroutine { continuation ->
        val promise: Promise<T> = callAsyncJsFunctionWithSpreadOperator<T>(asyncJsFunction, *args)
        promise
            .then { result ->
                continuation.resumeWith(Result.success(result))
            }
            .catch { error ->
                continuation.resumeWith(Result.failure(error))
            }
    }
}

/*internals*/

private fun <T> callAsyncJsFunctionWithSpreadOperator(jsFunction: dynamic, vararg args: String): Promise<T> {
    return when (args.size) {
        0 -> jsFunction()
        1 -> jsFunction(args[0])
        2 -> jsFunction(args[0], args[1])
        3 -> jsFunction(args[0], args[1], args[2])
        4 -> jsFunction(args[0], args[1], args[2], args[3])
        5 -> jsFunction(args[0], args[1], args[2], args[3], args[4])
        6 -> jsFunction(args[0], args[1], args[2], args[3], args[4], args[5])
        7 -> jsFunction(args[0], args[1], args[2], args[3], args[4], args[5], args[6])
        8 -> jsFunction(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
        else -> error("jsFunction: $jsFunction")
    } as Promise<T>
}

/*internals*/

private fun findFunction(functionPath: String): dynamic {
    var current: dynamic = jsWindow
    for (token in functionPath.split(".")) {
        current = current[token]
    }
    return current
}
