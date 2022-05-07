package featurea.utils

import featurea.utils.Encoding.*

enum class Encoding {
    BIG_ENDIAN,
    LITTLE_ENDIAN
}

fun Int.toFourBytes(): ByteArray {
    val result = ByteArray(4) { 0 }
    for (index in 0 until 4) result[index] = (this ushr (index * 8) and 255).toByte()
    return result
}

fun Short.encodeToTwoBytes(encoding: Encoding): ByteArray = when (encoding) {
    BIG_ENDIAN -> byteArrayOf((toInt() ushr 8 and 255).toByte(), (toInt() ushr 0 and 255).toByte())
    LITTLE_ENDIAN -> byteArrayOf((toInt() ushr 0 and 255).toByte(), (toInt() ushr 8 and 255).toByte())
}

typealias TwoBytes = ByteArray

@OptIn(ExperimentalUnsignedTypes::class)
fun TwoBytes.encodeToShort(encoding: Encoding): Short {
    val big: Byte = this[0]
    val little: Byte = this[1]
    return when (encoding) {
        BIG_ENDIAN -> {
            val result: Int = (big.toUByte().toInt() shl 8) + (little.toUByte().toInt() shl 0)
            result.toShort()
        }
        LITTLE_ENDIAN -> {
            val result: Int = (little.toUByte().toInt() shl 0) + (big.toUByte().toInt() shl 8)
            result.toShort()
        }
    }

}

fun Float.encodeToTwoShorts(encoding: Encoding): TwoShorts = toBits().encodeToTwoShorts(encoding)

@OptIn(ExperimentalUnsignedTypes::class)
fun Int.encodeToTwoShorts(encoding: Encoding): TwoShorts {
    val fourBytes: ByteArray = toFourBytes()
    val b0: Int = fourBytes[0].toUByte().toInt() shl 8
    val b1: Int = fourBytes[1].toUByte().toInt()
    val little: Short = (b0 + b1).toShort()
    val b2: Int = fourBytes[2].toUByte().toInt() shl 8
    val b3: Int = fourBytes[3].toUByte().toInt()
    val big: Short = (b2 + b3).toShort()
    return when (encoding) {
        BIG_ENDIAN -> shortArrayOf(big, little)
        LITTLE_ENDIAN -> shortArrayOf(little, big)
    }
}

typealias TwoShorts = ShortArray

@OptIn(ExperimentalUnsignedTypes::class)
fun TwoShorts.encodeToInt(encoding: Encoding): Int {
    val first: Short = this[0]
    val second: Short = this[1]
    val (first0, first1) = first.encodeToTwoBytes(encoding)
    val (second0, second1) = second.encodeToTwoBytes(encoding)
    return when (encoding) {
        BIG_ENDIAN -> {
            (first0.toUByte().toInt() shl (3 * 8)) + (first1.toUByte().toInt() shl (2 * 8)) +
                    (second0.toUByte().toInt() shl (1 * 8)) + (second1.toUByte().toInt() shl (0 * 8))
        }
        LITTLE_ENDIAN -> {
            (second0.toUByte().toInt() shl (3 * 8)) + (second1.toUByte().toInt() shl (2 * 8)) +
                    (first0.toUByte().toInt() shl (1 * 8)) + (first1.toUByte().toInt() shl (0 * 8))
        }
    }
}

fun TwoShorts.encodeToFloat(encoding: Encoding): Float = Float.fromBits(encodeToInt(encoding))

fun ShortArray.encodeToByteArray(encoding: Encoding): ByteArray {
    val result = ByteArray(size * 2) { 0 }
    for ((index, short) in this.withIndex()) {
        val twoBytes = short.encodeToTwoBytes(encoding)
        result[2 * index] = twoBytes[0]
        result[2 * index + 1] = twoBytes[1]
    }
    return result
}

fun ByteArray.encodeToShortArray(encoding: Encoding): ShortArray {
    val twoBytes = byteArrayOf(0, 0)
    val result = ShortArray(size / 2) { 0 }
    for (index in indices step 2) {
        twoBytes[0] = this[index]
        twoBytes[1] = this[index + 1]
        result[index / 2] = twoBytes.encodeToShort(encoding)
    }
    return result
}