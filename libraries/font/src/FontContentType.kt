package featurea.font

import featurea.content.ContentType
import featurea.content.Resource
import featurea.content.ResourceNotFoundException
import featurea.font.reader.fnt
import featurea.font.reader.png
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.text.TextContent
import featurea.image.ImageContentType
import featurea.spritesheet.SpriteCache
import featurea.spritesheet.Sprite

class FontContentType(override val module: Module) : Component, ContentType {

    private val imageContentType: ImageContentType = import()
    private val textContent: TextContent = import()
    private val spriteCache: SpriteCache = import()

    val fontCache = mutableMapOf<String, Font>()

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        // 1. load font
        val font: Font = run {
            if (fontCache[resource.path] == null) {
                val fntPath: String = resource.manifest.fnt
                val fntSource: String = textContent.findTextOrNull(fntPath) ?: throw ResourceNotFoundException(fntPath)
                val font: Font = FontFileParser.parseFontSource(fntSource, fntPath)
                fontCache[resource.path] = font
            }
            fontCache[resource.path] ?: error("resource: $resource")
        }
        loadingQueue.add(resource.path)

        // 2. load image
        val imagePath: String = resource.manifest.png
        imageContentType.loadSprite(imagePath, loadingQueue)
        val textureRegion: Sprite = spriteCache.sprites[imagePath] ?: error("resource: $resource")
        font.initTextureRegion(textureRegion)
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        // 1. release font
        fontCache.remove(resource.path)
        releaseQueue.add(resource.path)
        textContent.removeCachedText(resource.manifest.fnt)

        // 2. release texture
        imageContentType.releaseSprite(resource.manifest.png, releaseQueue)
    }

}
