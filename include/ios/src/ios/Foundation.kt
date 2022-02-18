package featurea.ios

import kotlinx.cinterop.*
import platform.Foundation.*

val documentsDir: String = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)[0] as String

val fileManager: NSFileManager get() = NSFileManager.defaultManager

fun NSFileManager.contentsOfDirectoryAtURL(url: NSURL): List<NSURL> =
    contentsOfDirectoryAtURL(url, null, NSDirectoryEnumerationSkipsHiddenFiles, null) as List<NSURL>

fun NSFileManager.contentsOfDirectoryAtURL(filePath: String): List<NSURL> =
    contentsOfDirectoryAtURL(NSURL(fileURLWithPath = filePath))

fun NSFileManager.createDirectoryAtPath(dirPath: String) {
    if (!fileExistsAtPath(dirPath)) {
        createDirectoryAtPath(path = dirPath, withIntermediateDirectories = true, attributes = null, error = null)
    }
}

fun NSURL.isDirectory(): Boolean = memScoped {
    return alloc<ObjCObjectVar<Any?>>().also { getResourceValue(it.ptr, NSURLIsDirectoryKey, null) }.value as Boolean
}

fun NSURL.isFile(): Boolean = !isDirectory()

fun NSError.toException(): Exception = Exception(localizedDescription)

fun ByteArray.toNSData(): NSData {
    val data: ByteArray = this
    memScoped {
        return NSData.create(bytes = allocArrayOf(data), length = size.toULong())
    }
}
