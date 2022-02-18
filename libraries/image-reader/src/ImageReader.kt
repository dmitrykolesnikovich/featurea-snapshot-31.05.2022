package featurea.image.reader

import featurea.*
import featurea.content.*
import featurea.runtime.Container
import featurea.spritesheet.*
import featurea.text.TextContent

class ImageReader(container: Container) : ResourceReader {

    private val system: System = container.import()
    private val textContent: TextContent = container.import()
    private val spriteCache: SpriteCache = container.import()
    private val spritesheetReader: SpritesheetReader = container.import()

    private val missingManifestCache = mutableSetOf<String>()

    override suspend fun createIfAbsent(resourcePath: String) {
        // println("[ImageReader.kt] createIfAbsent: $resourcePath")
        if (resourcePath.isValidFilePath() && resourcePath.extension == gifExtension) {
            val resourceDir: String = "$GIF_CACHE_PATH/${resourcePath.normalizedPath.removePrefix("/")}"
            // println("[ImageReader.kt] resourceDir: $resourcePath")
            if (!system.existsFile(resourceDir)) {
                val absolutePath: String? = system.findAbsolutePathOrNull(resourcePath)
                // println("[ImageReader.kt] absolutePath: $absolutePath")
                if (absolutePath != null) {
                    val command: String = "extractGif '$absolutePath' '$resourcePath'"
                    // println("[ImageReader.kt] runCommand: $command")
                    runCommand(command, name = "Extracting GIF...")
                }
            }
        }
    }

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (system.useTexturePack) {
            spritesheetReader.init {
                for ((_, textureAtlas) in spriteCache.spritesheets) {
                    for ((imagePath, textureRegion) in textureAtlas.sprites) {
                        spriteCache.sprites[imagePath] = textureRegion
                    }
                }
            }
        }
        if (resourcePath.hasExtension(pngExtension, jpgExtension, jpegExtension) && resourcePath.isValidFilePath()) {
            if (system.useTexturePack) {
                val spritesheet: Spritesheet = spriteCache.sprites[resourcePath]?.spritesheet ?: return null
                return Resource(spritesheet.spritePath, TEXTURES_PACK_PATH) {
                    Properties("atlas" to spritesheet.spritePath, "pack" to TEXTURES_PACK_PATH)
                }
            } else {
                return Resource(resourcePath) {
                    Properties("texture" to resourcePath)
                }
            }
        }
        if (resourcePath.extension == gifExtension && resourcePath.isValidFilePath()) {
            val resourceDir: String = "$GIF_CACHE_PATH/${resourcePath.normalizedPath.removePrefix("/")}"
            val manifestPath: String = "${resourceDir}/manifest.properties"
            val properties: Properties = Properties().apply {
                val text: String? = textContent.findTextOrNull(manifestPath)
                val properties: Map<String, Any> = when {
                    text != null -> parseProperties(text)
                    else -> {
                        if (missingManifestCache.add(manifestPath)) throw ResourceNotFoundException(manifestPath)
                        mutableMapOf(
                            "fps" to 60,
                            "frameCount" to 0,
                            "frames" to emptyList<String>(),
                            "loopCount" to 1
                        )
                    }
                }
                putAll(properties)
            }
            val resources = mutableListOf<String>()
            for (index in 0 until properties.frameCount) {
                resources.add("${resourceDir}/${index}.png")
            }
            if (system.useTexturePack) {
                val frames: ArrayList<String> = ArrayList<String>().apply {
                    add(TEXTURES_PACK_PATH)
                    addAll(resources.map { spriteCache.findSpritesheet(it).spritePath })
                }
                return Resource(frames, manifestPath) { properties }
            } else {
                return Resource(resources, manifestPath) { properties }
            }
        }
        return null
    }

}
