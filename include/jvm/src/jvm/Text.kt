package featurea.jvm

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.zip.ZipInputStream

fun String.convertToUtf8(): String = URLDecoder.decode(this, "UTF-8")

fun Map<String, Any?>.writeText(file: File): Unit = file.writeText(text())

fun Map<String, Any?>.text(): String = text("=")

fun Map<String, Any?>.text(delimiter: String): String {
    val stringBuilder = StringBuilder()
    for ((key, value) in this) {
        val string = when(value) {
            is List<*> -> value.joinToString()
            is Map<*, *> -> value.entries.joinToString()
            else -> value
        }
        stringBuilder.append(key).append(delimiter).append(string).appendLine()
    }
    return stringBuilder.toString()
}

fun ZipInputStream.toText(): String {
    val stringBuilder = StringBuilder()
    val reader = InputStreamReader(this, Charsets.UTF_8)
    val buffer = CharArray(1024)
    while (true) {
        val count = reader.read(buffer)
        if (count == -1) break
        stringBuilder.append(buffer, 0, count)
    }
    return stringBuilder.toString()
}

fun InputStream.toText(): String {
    val stringBuilder = StringBuilder()
    val bufferedReader = BufferedReader(InputStreamReader(this, Charsets.UTF_8))
    while (true) {
        val line = bufferedReader.readLine()
        if (line == null) break
        stringBuilder.appendln(line)
    }
    return stringBuilder.toString()
}