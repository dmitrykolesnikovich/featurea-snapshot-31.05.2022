package featurea.utils

import featurea.Properties
import featurea.jvm.toText
import java.io.File
import java.io.InputStream

fun Properties(file: File): Properties {
    val inputStream = file.inputStream()
    return Properties(inputStream)
}

fun Properties(filePath: String): Properties {
    val inputStream = ClassLoader.getSystemResourceAsStream(filePath) ?: error("resourcePath: $filePath")
    return Properties(inputStream)
}

fun Properties(inputStream: InputStream): Properties {
    val source = inputStream.toText()
    val original = parseProperties(source)
    return Properties(original)
}
