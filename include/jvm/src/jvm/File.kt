package featurea.jvm

import featurea.System
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.jar.JarFile


val String.extension: String
    get() = @Suppress("DefaultLocale") substringAfterLast(".").toLowerCase()

fun String.createNewFileAndDirs(): File {
    val path = this
    File(path).parentFile.mkdirs()
    val newFile = File(path)
    if (!newFile.exists()) {
        newFile.createNewFileAndDirs()
    }
    return newFile
}

fun File.createIfNotExists(): Boolean {
    return if (exists()) false else createNewFileAndDirs()
}

fun File.createNewFileAndDirs(): Boolean {
    println("[File.kt] createNewFileAndDirs: $absolutePath")
    if (name.isEmpty()) {
        return false
    }
    try {
        parentFile?.mkdirs()
        return createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
}

fun System.relativeTo(filePath: String): String {
    val absolutePath = findFile(filePath).absolutePath.replace("\\", "/")
    for (contentRoot in contentRoots) {
        if (absolutePath.startsWith(contentRoot)) {
            return if (contentRoot.isEmpty()) {
                absolutePath
            } else {
                absolutePath.replaceFirst("${contentRoot}/", "")
            }
        }
    }
    error("filePath: $filePath")
}

// quickfix todo improve
fun System.readBytes(filePath: String): ByteArray? {
    if (filePath.endsWith(".shader")) {
        println("breakpoint")
    }
    return readInputStreamOrNull(filePath)?.readBytes()
}

fun System.findBufferedInputStream(filePath: String): BufferedInputStream =
    BufferedInputStream(readInputStreamOrNull(filePath))

fun System.cacheIfNotExists(internalPath: String, externalPath: String) {
    cacheIfNotExists(internalPath, File(externalPath))
}

fun System.cacheIfNotExists(internalPath: String, externalFile: File) = runBlocking {
    if (externalFile.exists()) return@runBlocking

    val inputStream = readInputStreamOrNull(internalPath) ?: error("internalPath: $internalPath")
    externalFile.parentFile.mkdirs()
    inputStream.copyTo(FileOutputStream(externalFile))
}

val File.normalizedPath: String get() = path.replace("\\", "/")

val JarFile.normalizedPath: String get() = name.replace("\\", "/")
