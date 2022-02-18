package featurea.image

import featurea.opengl.Texture
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.spritesheet.Spritesheet
import featurea.spritesheet.SpriteCache

class ImageContent(override val module: Module) : Component {

    private val spriteCache: SpriteCache = import()

    private val images = mutableMapOf<String, Image>()

    operator fun set(key: String, image: Image) {
        images[key] = image
    }

    operator fun get(key: String): Image? {
        return images[key]
    }

    fun findImage(spritePath: String?): Image {
        return findImageOrNull(spritePath) ?: error("spritePath: $spritePath")
    }

    fun findImageOrNull(spritePath: String?): Image? {
        if (spritePath == null) return null
        val spritesheet: Spritesheet = spriteCache.findSpritesheet(spritePath)
        val image: Image? = images[spritesheet.spritePath]
        return image
    }

    fun findTexture(spritePath: String?): Texture {
        return findImage(spritePath).texture
    }

    fun findTextureOrNull(spritePath: String?): Texture? {
        return findImageOrNull(spritePath)?.texture
    }

    fun remove(spritePath: String): Image? {
        return images.remove(spritePath)
    }

}
