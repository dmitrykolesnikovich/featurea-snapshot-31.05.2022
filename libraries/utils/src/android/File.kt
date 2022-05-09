@file:JvmName("FileUtils")

package featurea.utils

import featurea.jvm.findFileOrNull
import featurea.jvm.toText
import java.io.File
import java.util.zip.ZipFile
import featurea.System

actual fun existsFile(filePath: String): Boolean {
    return File(filePath).exists()
}

actual fun System.existsFile(filePath: String): Boolean {
    return findFileOrNull(filePath) != null
}

actual fun System.findAbsolutePathOrNull(filePath: String): String? {
    return findFileOrNull(filePath)?.absolutePath
}

actual suspend fun System.readTextOrNull(filePath: String, limit: Int): String? {
    for (contentRoot in contentRoots) {
        val text: String? = readTextUtf8(contentRoot, filePath, limit)
        if (text != null) {
            return text
        }
    }
    return null
}

// todo make use of `limit`
private fun System.readTextUtf8(contentRoot: String, filePath: String, limit: Int): String? {
    // 1. FAB inside APK
    /*if (bundle.isInternal) {
        val result = tryReadBundle(contentRoot, "assets/${bundle.path}", filePath)
        if (result != null) {
            return result.toText()
        }
    }*/

    // 2. APK or FAB
    val zipFile = ZipFile(File(contentRoot))
    val zipEntry = zipFile.getEntry("assets/$filePath") ?: zipFile.getEntry(filePath)
    if (zipEntry != null) {
        val inputStream = zipFile.getInputStream(zipEntry)
        return inputStream.toText()
    }

    // 3. file not found
    return null
}
