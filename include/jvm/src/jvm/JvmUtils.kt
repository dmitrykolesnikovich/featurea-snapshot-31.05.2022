package featurea.jvm

import java.io.File
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.lang.System as JvmSystem

private val doubleToStringConverter = DecimalFormat("#.############", DecimalFormatSymbols.getInstance(Locale.ENGLISH))

fun Double.toDoubleString(): String = doubleToStringConverter.format(this)

fun ByteArray.toHexString(): String {
    val result = StringBuilder()
    for (byte in this) {
        result.append(String.format("%02X", byte.toInt() and 0xFF))
    }
    return result.toString()
}

fun StringBuilder.replaceAll(byteBuffer: ByteBuffer, size: Int): String {
    val array = ByteArray(size)
    byteBuffer.get(array, 0, size)
    clear()
    for (index in 0 until size) append(array[index].toChar())
    return toString()
}

fun existingFileOrNull(filePath: String): File? {
    val file: File = File(filePath)
    if (file.exists()) {
        return file
    } else {
        return null
    }
}
