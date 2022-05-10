package featurea.font.reader

import featurea.Bundle
import featurea.utils.FONT_CACHE_PATH
import featurea.Properties
import featurea.System
import featurea.utils.runCommand
import featurea.content.Resource
import featurea.content.ResourceReader
import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import
import featurea.text.TextContent
import featurea.spritesheet.TEXTURES_PACK_PATH
import featurea.spritesheet.Spritesheet
import featurea.spritesheet.SpriteCache
import featurea.spritesheet.useTexturePack

class FontReader(override val module: Module) : Component, ResourceReader {

    private val spriteCache: SpriteCache = import()
    private val system: System = import()
    private val textContent: TextContent = import()

    override suspend fun createIfAbsent(resourcePath: String) {
        if (resourcePath.startsWith(FONT_TYPE_PREFIX)) {
            val path: String = resourcePath.removePrefix(FONT_TYPE_PREFIX)
            val fntPath: String = "$FONT_CACHE_PATH/${path}.fnt"
            val fontProperties: Map<String, Any> = resourcePath.toFontProperties()
            val name: String = fontProperties["name"] as String
            val size: Int = fontProperties["size"] as Int
            val isBold: Boolean = fontProperties["bold"] as Boolean
            val isItalic: Boolean = fontProperties["italic"] as Boolean
            if (textContent.findTextOrNull(fntPath) == null) {
                runCommand("createFont '$fntPath' '${name}' $size $isBold $isItalic", name = "Creating Font...")
            }
        }
    }

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.startsWith(FONT_TYPE_PREFIX)) {
            val path: String = resourcePath.removePrefix(FONT_TYPE_PREFIX)
            val fntPath: String = "$FONT_CACHE_PATH/${path}.fnt"
            val pngPath: String = "$FONT_CACHE_PATH/${path}.png"
            if (system.useTexturePack) {
                val spritesheet: Spritesheet = spriteCache.findSpritesheet(pngPath)
                return Resource(fntPath, spritesheet.spritePath, TEXTURES_PACK_PATH) {
                    Properties(
                        "atlas" to spritesheet.spritePath,
                        "fnt" to fntPath,
                        "pack" to TEXTURES_PACK_PATH,
                        "png" to pngPath,
                    )
                }
            } else {
                return Resource(fntPath, pngPath) {
                    Properties(
                        "fnt" to fntPath,
                        "png" to pngPath,
                    )
                }
            }
        }
        return null
    }

}
