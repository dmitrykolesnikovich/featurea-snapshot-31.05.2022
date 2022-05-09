@file:Suppress("EXPERIMENTAL_API_USAGE", "CAST_NEVER_SUCCEEDS", "EXPERIMENTAL_IS_NOT_ENABLED")

package featurea.utils

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

// todo add more encodings
actual fun String.encodeToByteArray(charset: String): ByteArray {
    val string: NSString = this as NSString
    val encoding = when (charset) {
        "ISO-8859-1" -> NSISOLatin1StringEncoding
        "UTF8" -> NSUTF8StringEncoding
        else -> NSUTF8StringEncoding
    }
    val data: NSData = string.dataUsingEncoding(encoding = encoding, allowLossyConversion = true)!!
    val result = data.toByteArray()
    return result
}

// https://github.com/JetBrains/kotlin-native/issues/3172#issuecomment-510051810
@OptIn(ExperimentalUnsignedTypes::class)
fun NSData.toByteArray() = ByteArray(length.toInt()).apply { usePinned { memcpy(it.addressOf(0), bytes, length) } }
