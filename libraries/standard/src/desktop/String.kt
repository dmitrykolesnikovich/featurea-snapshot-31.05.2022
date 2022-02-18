@file:JvmName("StringUtils")

package featurea

import featurea.jvm.isWindows
import java.nio.charset.Charset

actual fun String.encodeToByteArray(charset: String): ByteArray = toByteArray(Charset.forName(charset))

fun String.correctPathDelimiter(): String {
    return if (isWindows) replace("/", "\\") else this // quickfix todo find better place
}
