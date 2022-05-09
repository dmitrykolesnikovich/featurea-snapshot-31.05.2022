package featurea.packTextures

import featurea.utils.parent
import featurea.runtime.containerScope
import java.io.File

fun main(vararg args: String) {
    check(args.size >= 4)
    containerScope(artifact) {
        val texturePacker: TexturePacker = import()
        var previousDir: String? = null
        for (index in 0 until args.size - 2 step 2) {
            val arg0 = args[index]
            val frameName = if (arg0.startsWith("...")) arg0.replaceFirst("...", previousDir!!) else arg0
            previousDir = frameName.parent
            val shortenFilePath = args[index + 1]
            val filePath = if (shortenFilePath != ".") if (shortenFilePath.endsWith("/...")) {
                shortenFilePath.replace("/...", "/$frameName")
            } else {
                shortenFilePath
            } else {
                frameName
            }
            texturePacker.addImage(frameName, File(filePath))
        }
        texturePacker.pack(File(args[args.size - 2]), args[args.size - 1])
    }
}
