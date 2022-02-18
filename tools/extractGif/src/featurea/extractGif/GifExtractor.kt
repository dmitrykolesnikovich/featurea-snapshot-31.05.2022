package featurea.extractGif

import featurea.jvm.userHomePath
import featurea.normalizedPath
import java.io.BufferedInputStream
import java.io.File

public class GifExtractor {

    fun extractGifFile(gifFile: File, filePath: String): File {
        val normalizedPath = filePath.normalizedPath
        val outputDir = File("$userHomePath/.featurea/cache/gifs/${normalizedPath}")
        GifFramesExtractor.getFramesDirForGif(BufferedInputStream(gifFile.inputStream()), outputDir)
        return outputDir
    }

}
