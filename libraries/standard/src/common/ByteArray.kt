package featurea

fun String.encodeToShortArray(count: Int): ShortArray {
    val bytes: ByteArray = encodeToByteArray("ISO-8859-1")
    val result = ShortArray(count)
    var index = 0
    while (index < bytes.size) {
        val byte1: Byte = bytes[index];
        val byte2: Byte = if (index + 1 < bytes.size) bytes[index + 1] else 0
        val integer = (((byte1.toInt() and 0xff) shl 8) or (byte2.toInt() and 0xff));
        result[index / 2] = integer.toShort()
        index += 2
    }
    return result;
}

expect fun String.encodeToByteArray(charset: String): ByteArray
