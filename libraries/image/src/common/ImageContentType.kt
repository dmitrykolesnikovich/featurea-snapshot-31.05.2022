package featurea.image

import featurea.*
import featurea.content.*
import featurea.image.reader.texture
import featurea.opengl.*
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.spritesheet.Spritesheet
import featurea.spritesheet.SpriteCache
import featurea.spritesheet.frameCount
import featurea.spritesheet.useTexturePack

class ImageContentType(override val module: Module) : Component, ContentType {

    private val gl: Opengl = import(OpenglProxy)
    private val imageContent: ImageContent = import()
    private val imageLoader: ImageLoader = import()
    private val spriteCache: SpriteCache = import()
    private val system: System = import()

    override suspend fun load(resource: Resource, loadingQueue: ArrayList<String>) {
        if (resource.path.extension == gifExtension) {
            if (system.useTexturePack) {
                val resourceDir: String = "$GIF_CACHE_PATH/${resource.path.normalizedPath.removePrefix("/")}"
                val spritePaths: MutableList<String> = mutableListOf<String>()
                for (index in 0 until resource.manifest.frameCount) {
                    spritePaths.add("${resourceDir}/${index}.png")
                }
                for (spritePath in spritePaths) {
                    loadSprite(spritePath, loadingQueue)
                }
            } else {
                for (file in resource.files) {
                    loadSprite(file, loadingQueue)
                }
            }
            loadingQueue.add(resource.path)
        }
        if (resource.path.hasExtension(jpegExtension, jpgExtension, pngExtension)) {
            if (system.useTexturePack) {
                loadSprite(resource.path, loadingQueue)
            } else {
                loadSprite(resource.manifest.texture, loadingQueue)
            }
        }
    }

    override suspend fun release(resource: Resource, releaseQueue: ArrayList<String>) {
        if (resource.path.extension == gifExtension) {
            for (file in resource.files) {
                releaseSprite(file, releaseQueue)
            }
        }
        if (resource.path.hasExtension(jpegExtension, jpgExtension, pngExtension)) {
            releaseSprite(resource.manifest.texture, releaseQueue)
        }
    }

    suspend fun loadSprite(spritePath: String, loadingQueue: MutableList<String>) {
        val spritesheet: Spritesheet = spriteCache.findSpritesheet(spritePath)
        if (imageContent[spritesheet.spritePath] == null) {
            val texture: Texture = gl.createTexture(spritePath)
            val image: Image = Image(spritesheet, texture)
            gl.bindTexture(TEXTURE_2D, image.texture)
            gl.textureParameter(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR_MIPMAP_LINEAR)
            gl.textureParameter(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR)
            gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
            gl.textureParameter(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
            imageLoader.loadImage(image)
            spriteCache.cacheSpritesheet(spritesheet)
            gl.generateMipmap(TEXTURE_2D)
            gl.bindTexture(TEXTURE_2D, null)
            imageContent[spritesheet.spritePath] = image
        }
        loadingQueue.addAll(spritesheet.spritePaths)
    }

    fun releaseSprite(spritePath: String, releaseQueue: MutableList<String>) {
        val spritesheet: Spritesheet? = spriteCache.spritesheets[spritePath]
        if (spritesheet != null) {
            releaseQueue.addAll(spritesheet.spritePaths)
            val image: Image? = imageContent.remove(spritesheet.spritePath)
            if (image != null) {
                spriteCache.removeSpritesheet(image.spritesheet)
                gl.deleteTexture(image.texture)
            }
        } else {
            releaseQueue.add(spritePath)
        }
    }

}
