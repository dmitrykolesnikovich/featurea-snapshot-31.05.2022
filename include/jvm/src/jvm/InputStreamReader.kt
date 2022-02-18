package featurea.jvm

import featurea.System
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun System.readInputStreamOrNull(filePath: String): InputStream? {
    // println("[InputStreamReader.kt] entering")
    for (contentRoot in contentRoots) {
        if (filePath == contentRoot) continue
        // println("[InputStreamReader.kt] contentRoot: $contentRoot")
        val extension: String = contentRoot.extension

        // existing file
        val file: File = File("${contentRoot}/${filePath}")
        // println("[InputStreamReader.kt] $file: ${file.exists()}")
        if (file.exists()) {
            return FileInputStream(file)
        }

        // zip entry
        if (extension.isZipFileExtension()) {
            val zipFile: ZipFile = ZipFile(contentRoot)
            val zipEntry: ZipEntry? = zipFile.getEntry(filePath.toZippedEntryPath())
            if (zipEntry != null) {
                val inputStream: InputStream = zipFile.getInputStream(zipEntry)
                // println("[InputStreamReader.kt] inputStream: $inputStream")
                if (filePath.needsToBeExtracted()) {
                    val extractedFile: File = File(contentRoot.toExtractedDir(), filePath)
                    runBlocking {
                        extractedFile.delete()
                        zipFile.getInputStream(zipEntry).copyTo(extractedFile)
                    }
                }
                return inputStream
            }
        }
    }

    // default
    val inputStream: InputStream? = ClassLoader.getSystemClassLoader().getResourceAsStream(filePath)
    return inputStream
}
