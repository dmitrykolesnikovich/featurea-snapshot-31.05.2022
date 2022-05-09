@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package featurea.utils

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import featurea.System

actual fun existsFile(filePath: String): Boolean = TODO()

actual fun System.existsFile(filePath: String): Boolean = TODO()

actual fun System.findAbsolutePathOrNull(filePath: String): String? = TODO()

actual suspend fun System.readTextOrNull(filePath: String, limit: Int): String? {
    for (contentRoot in contentRoots) {
        val text: String? = readTextOrNull(contentRoot, filePath)
        if (text != null) {
            return text
        }
    }
    return null
}

/*internals*/

@OptIn(ExperimentalUnsignedTypes::class)
private fun System.readTextOrNull(contentRoot: String, filePath: String): String? {
    log("[File] readTextUtf8: $contentRoot, $filePath, $workingDir")

    // 1. FAB
    if (workingDir != null && workingDir == contentRoot) {
        val textPath: String = "${contentRoot}/${filePath}"
        val text: String? = NSString.stringWithContentsOfFile(textPath, NSUTF8StringEncoding, null)
        if (text != null) {
            return text
        }
    }

    // 2. IPA
    val name: String = "assets/${filePath}" // quickfix todo improve
    val path: String = NSBundle.mainBundle().pathForResource(name, null) ?: return null
    val text: String? = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
    return text
}
