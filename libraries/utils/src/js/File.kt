package featurea.utils

import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.suspendCoroutine
import featurea.System

actual fun existsFile(filePath: String): Boolean = TODO()

actual fun System.existsFile(filePath: String): Boolean = TODO()

actual fun System.findAbsolutePathOrNull(filePath: String): String? = TODO()

actual suspend fun System.readTextOrNull(filePath: String, limit: Int): String? {
    for (contentRoot in contentRoots) {
        val text: String? = requestTextOrNull(contentRoot, filePath, limit)
        if (text != null) {
            return text
        }
    }
    return null
}

/*internals*/

// todo make use of `limit`
private suspend fun System.requestTextOrNull(contentRoot: String, filePath: String, limit: Int): String? {
    val textUrl: String = if (filePath.startsWith("\\")) {
        filePath.removePrefix("\\")
    } else {
        "${workingDir ?: "bundle"}/${filePath.removePrefix("/")}"
    }.normalizedPath
    log("[File.kt] getText: $filePath")

    return suspendCoroutine { continuation ->
        try {
            val request: XMLHttpRequest = XMLHttpRequest()
            request.open("GET", textUrl, true)
            request.onload = {
                if (request.readyState == 4.toShort() && request.status == 200.toShort()) {
                    continuation.resumeWith(Result.success(request.responseText))
                } else {
                    continuation.resumeWith(Result.success(null))
                }
            }
            request.onerror = {
                continuation.resumeWith(Result.success(null))
            }
            request.send(null)
        } catch (e: Throwable) {
            e.printStackTrace()
            continuation.resumeWith(Result.success(null))
        }
    }
}
