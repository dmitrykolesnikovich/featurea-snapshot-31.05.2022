package featurea.jvm

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

val Class<*>.distPath: String?
    get() {
        val protectionDomain = protectionDomain ?: return null
        val codeSource = protectionDomain.codeSource ?: return null
        val location = codeSource.location ?: return null
        return location.path.replace("\\", "/")
    }

fun File.unzipTo(destDir: File) {
    val zipFile = this

    // 1. create output directory if it doesn't exist
    if (!destDir.exists()) {
        destDir.mkdirs()
    }

    // 2. buffer for read and write data to file
    val fis: FileInputStream
    val buffer = ByteArray(1024)
    try {
        fis = FileInputStream(zipFile)
        val zis = ZipInputStream(fis)
        var ze = zis.nextEntry
        while (ze != null) {
            val name = ze.name
            val newFile = File(destDir, name.replace("\\", "/").replace(":", ""))
            // println("Unzipping to " + newFile.absolutePath)
            // create directories for sub directories in zip
            if (ze.isDirectory) {
                newFile.mkdirs()
            } else {
                File(newFile.parent).mkdirs()
                val fos = FileOutputStream(newFile)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
            }
            // close this ZipEntry
            zis.closeEntry()
            ze = zis.nextEntry
        }
        // close last ZipEntry
        zis.closeEntry()
        zis.close()
        fis.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun Class<*>.findZipFileOrNull(): File? {
    val type = this
    val filePath = type.distPath?.convertToUtf8()?.replace("file://", "")
    if (filePath != null && (filePath.endsWith(".jar") || filePath.endsWith(".exe"))) {
        val file = File(filePath)
        if (file.exists()) {
            return file
        }
    }
    return null
}

fun readZipFileOrNull(zipFile: String, bundlePath: String, filePath: String): ZipInputStream? {
    try {
        val first = ZipInputStream(FileInputStream(zipFile))
        val second = first.getInputStream(bundlePath) ?: return null
        val third = ZipInputStream(second).getInputStream(filePath) ?: return null
        return third
    } catch (e: Throwable) {
        // quickfix todo avoid throwing exception
        return null
    }
}

/*internals*/

private fun ZipInputStream.getInputStream(name: String?): ZipInputStream? {
    while (true) {
        val entry: ZipEntry = nextEntry ?: return null
        if (entry.name == name) return this
    }
}
