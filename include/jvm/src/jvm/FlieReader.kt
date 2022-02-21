package featurea.jvm

import featurea.System
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun System.findFile(filePath: String): File {
    return findFileOrNull(filePath) ?: throw FileNotFoundException(filePath)
}

fun System.findFileOrNull(filePath: String): File? {
    // println("[FileReader.kt] contentRoots: ${contentRoots.joinToString()}")
    for (contentRoot in contentRoots) {
        if (filePath == contentRoot) continue
        val extension: String = contentRoot.extension

        // existing file
        val file: File = File("${contentRoot}/${filePath}")
        // println("[FileReader.kt] existing: ${file.absolutePath} (${file.exists()})")
        if (file.exists()) {
            return file
        }

        // zip entry
        if (extension.isZipFileExtension()) {
            if (filePath.needsToBeExtracted()) {
                val zipFile: ZipFile = ZipFile(File(contentRoot))
                val zipEntry: ZipEntry? = zipFile.getEntry(filePath.toZippedEntryPath(extension))
                if (zipEntry != null) {
                    val extractedFile: File = File(contentRoot.toExtractedDir(), filePath)
                    runBlocking {
                        extractedFile.delete()
                        zipFile.getInputStream(zipEntry).copyTo(extractedFile)
                    }
                    return extractedFile
                }
            }
        }
    }
    return null
}
