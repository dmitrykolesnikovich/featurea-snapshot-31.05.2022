@file:JvmName("FileUtils")

package featurea

import featurea.jvm.findFile
import featurea.jvm.findFileOrNull
import featurea.jvm.toText
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

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
        val text: String? = readTextUtf8OrNull(contentRoot, filePath, limit)
        if (text != null) {
            return text
        }
    }
    return null
}

// quickfix todo improve
// todo make use of `limit`
private fun System.readTextUtf8OrNull(contentRoot: String, filePath: String, limit: Int): String? {
    val textPath = if (contentRoot.isNotEmpty()) "${contentRoot}/${filePath.removePrefix("/")}" else filePath

    // 1. dev
    val file: File = File(textPath)
    if (file.exists()) {
        return file.readText()
    }

    // 2. zip
    try {
        val zipFile: ZipFile = ZipFile(findFile(contentRoot))
        val zipEntry: ZipEntry = zipFile.getEntry(filePath)
        val inputStream: InputStream = zipFile.getInputStream(zipEntry)
        return inputStream.toText()
    } catch (skip: Throwable) {
        // no op
    }

    // 3. bin
    val inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(textPath) ?: return null
    val text = inputStream.toText()
    return text
}
