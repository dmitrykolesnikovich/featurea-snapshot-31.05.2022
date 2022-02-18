package featurea.fileTransfer

import featurea.jvm.createNewFileAndDirs
import featurea.normalizedPath
import java.io.*

fun newFile(filePath: String): File {
    return File(filePath.normalizedPath)
}

fun copy(from: InputStream, to: File) {
    try {
        if (to.exists()) {
            to.delete()
        }
        if (!to.exists()) {
            val path = to.absolutePath.normalizedPath
            val index = path.lastIndexOf("/")
            val dir = path.substring(0, index)
            val dirFile = File(dir)
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    System.err.println("$dirFile can not be created")
                }
            }
            to.createNewFileAndDirs()
        }
        var count: Int
        val outputStream: OutputStream = FileOutputStream(to)
        try {
            val buffer = ByteArray(1024)
            while (from.read(buffer).also { count = it } != -1) {
                outputStream.write(buffer, 0, count)
            }
        } finally {
            outputStream.flush()
            outputStream.close()
            from.close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
