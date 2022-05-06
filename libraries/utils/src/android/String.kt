@file:JvmName("StringUtils")

package featurea.utils

import java.nio.charset.Charset

actual fun String.encodeToByteArray(charset: String): ByteArray = toByteArray(Charset.forName(charset))
