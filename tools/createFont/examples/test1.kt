package featurea.createFont.examples

import featurea.createFont.FontCreator
import featurea.runtime.DefaultRuntime
import featurea.runtime.import
import java.io.File

fun test1() = DefaultRuntime(featurea.createFont.artifact) {
    val fontCreator: FontCreator = import()
    val fntFile = File("/Users/dmitrykolesnikovich/.featurea/cache/fonts/arial16.fnt")
    fontCreator.createFont(fntFile, "arial", 16, isBold = false, isItalic = false)
}
