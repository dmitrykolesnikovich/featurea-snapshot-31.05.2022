package featurea.jvm

import java.io.File

val userHomePath: String = java.lang.System.getProperty("user.home")!!.replace("\\", "/")
val userHomeDir: File = File(userHomePath)
val isAndroidJvm: Boolean = System.getProperty("java.runtime.name") == "Android Runtime"
val isLinux: Boolean = System.getProperty("os.name")!!.contains("Linux")
val isMacOs: Boolean = System.getProperty("os.name")!!.contains("Mac")
val isWindows: Boolean = System.getProperty("os.name")!!.contains("Windows")

fun String.isZipFileExtension(): Boolean = when {
    isAndroidJvm -> this == "apk"
    else -> this == "exe" || this == "bundle"
}

fun String.needsToBeExtracted(): Boolean = when {
    isAndroidJvm -> extension == "mp3"
    else -> true
}

fun String.toExtractedDir(): String = when {
    isAndroidJvm -> TODO()
    else -> "$userHomePath/.featurea/cache/extracted/${replace(":", "")}"
}

fun String.toZippedEntryPath(): String = when {
    isAndroidJvm -> "assets/$this"
    else -> this
}
