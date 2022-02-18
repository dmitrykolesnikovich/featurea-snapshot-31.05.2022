package featurea.createFont

import featurea.jvm.userHomePath
import featurea.runtime.containerScope
import java.io.File

fun main(vararg args: String) {
    check(args.size == 5)
    var fntFilePath = args[0]
    if (fntFilePath.startsWith(".featurea/cache/fonts/")) {
        fntFilePath = "$userHomePath/$fntFilePath"
    }
    val fntFile: File = File(fntFilePath)
    if (fntFile.exists()) return

    containerScope(artifact) {
        val fontCreator: FontCreator = import()
        val name = args[1]
        val size = args[2].toInt()
        val isBold = args[3].toBoolean()
        val isItalic = args[4].toBoolean()
        fontCreator.createFont(fntFile, name, size, isBold, isItalic)
    }
}
