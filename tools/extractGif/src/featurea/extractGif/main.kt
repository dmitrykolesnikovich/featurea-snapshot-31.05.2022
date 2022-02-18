package featurea.extractGif

import featurea.runtime.containerScope
import java.io.File

fun main(vararg args: String) {
    check(args.size == 1 || args.size == 2)
    lateinit var gifFile: File
    lateinit var filePath: String
    if (args.size == 1) {
        gifFile = File(args[0])
        filePath = args[0]
    } else if (args.size == 2) {
        gifFile = File(args[0])
        filePath = args[1]
    }
    check(gifFile.exists())

    containerScope(artifact) {
        val gifExtractor: GifExtractor = import()

        gifExtractor.extractGifFile(gifFile, filePath)
    }
}
