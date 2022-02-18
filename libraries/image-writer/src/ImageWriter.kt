package featurea.image.writer

import featurea.*
import featurea.runCommand
import featurea.content.*
import featurea.jvm.findFile
import featurea.jvm.findFileOrNull
import featurea.jvm.userHomePath
import featurea.runtime.Container
import featurea.spritesheet.TEXTURES_PACK_DIRECTORY_PATH
import featurea.spritesheet.TEXTURES_PACK_FILE_NAME
import featurea.spritesheet.useTexturePack
import featurea.packTextures.TexturePacker
import java.io.File
import kotlin.text.endsWith

class ImageWriter(container: Container) : ResourceWriter {

    private val content: Content = container.import()
    private val system: System = container.import()

    override suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        if (value.isValidImageResource()) {
            if (system.useTexturePack) {
                val file: File? = system.findFileOrNull(value)
                if (file != null) {
                    content.providedResources.add(value)
                    bundle.texturePack[value] = file.absolutePath
                }
            } else {
                content.providedResources.add(value)
            }
        }
        if (value.hasExtension(gifExtension)) {
            val gifFile: File? = system.findFileOrNull(value)
            if (gifFile != null) {
                content.providedResources.add(value)
                if (isInstrumentationEnabled && !File("$userHomePath/.featurea/cache/gifs/${value.normalizedPath}").exists()) {
                    runCommand("extractGif '${gifFile.absolutePath}' '$value'", name = "Loading GIF File...")
                }
                if (system.useTexturePack) {
                    val dir = "$GIF_CACHE_PATH/${value.normalizedPath.removePrefix("/")}"
                    val absoluteDir = "$userHomePath/${dir}"
                    val manifestFile = system.findFile("${absoluteDir}/manifest.properties")
                    val manifest = parseProperties(manifestFile.readText())
                    val frameCount = manifest["frameCount"]!!.toInt()
                    for (index in 0 until frameCount) {
                        val file = File("${absoluteDir}/${index}.png")
                        bundle.texturePack["${dir}/${index}.png"] = file.absolutePath
                    }
                }
            }
        }
    }

    override suspend fun flush(bundle: Bundle) {
        if (system.useTexturePack) {
            val outputDir: File = File("$userHomePath/$TEXTURES_PACK_DIRECTORY_PATH")
            File(outputDir, TEXTURES_PACK_FILE_NAME).deleteRecursively()
            // 1)
            if (bundle.texturePack.isNotEmpty()) {
                /*
                var previousDir: String? = null
                val args = bundle.texturePack.entries.joinToString(separator = " ") {
                    // 1)
                    val value = it.value.replace("\\", "/")
                    val shortenValue = if (value == it.key) "." else if (value.endsWith(it.key)) {
                        value.replace(it.key, "...")
                    } else {
                        value
                    }
                    val currentDir = it.key.parent
                    val frameName = if (previousDir == currentDir) {
                        it.key.replace(currentDir, "...")
                    } else {
                        previousDir = currentDir
                        it.key
                    }
                    "\"$frameName\" \"${shortenValue}\""

                    // 2)
                    "\"${File(it.key)}\" \"${it.value.replace("\\", "/")}\""
                }
                runCommand("packTextures $args ${outputDir.absolutePath} $TEXTURES_PACK_FILE_NAME")
                */

                // 2)
                val texturePacker = TexturePacker()
                for ((key, value) in bundle.texturePack) {
                    if (value.endsWith(".gif")) continue // quickfix
                    texturePacker.addImage(key, File(value))
                }
                texturePacker.pack(File(outputDir.absolutePath), TEXTURES_PACK_FILE_NAME)
            }
        }
    }

}
