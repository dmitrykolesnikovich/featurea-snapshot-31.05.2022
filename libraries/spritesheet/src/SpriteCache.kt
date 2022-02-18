package featurea.spritesheet

import featurea.System
import featurea.breakpoint
import featurea.runtime.Container

const val TEXTURES_PACK_DIRECTORY_PATH = ".featurea/cache/textures"
const val TEXTURES_PACK_FILE_NAME = "textures.pack"
const val TEXTURES_PACK_PATH = "$TEXTURES_PACK_DIRECTORY_PATH/$TEXTURES_PACK_FILE_NAME"

class SpriteCache(container: Container) {

    private val system: System = container.import()

    val spritesheets = linkedMapOf<String, Spritesheet>()
    val sprites = linkedMapOf<String, Sprite>()

    fun findSpritesheet(spritePath: String): Spritesheet {
        val sprite: Sprite? = sprites[spritePath]
        if (system.useTexturePack) {
            if (sprite == null) {
                breakpoint()
            }
            return sprite!!.spritesheet
        } else {
            if (sprite != null) {
                return sprite.spritesheet
            }
            val spritesheet: Spritesheet = Spritesheet(spritePath)
            spritesheets[spritePath] = spritesheet
            return spritesheet
        }
    }

    fun cacheSpritesheet(spritesheet: Spritesheet) {
        if (system.useTexturePack) {
            // no op
        } else {
            val sprite = Sprite(
                spritesheet = spritesheet,
                path = spritesheet.spritePath,
                x1 = 0f,
                y1 = 0f,
                x2 = spritesheet.size.width,
                y2 = spritesheet.size.height,
                u1 = 0f,
                v1 = 0f,
                u2 = 1f,
                v2 = 1f
            )
            spritesheet.sprites[spritesheet.spritePath] = sprite
            sprites[sprite.path] = sprite
        }
    }

    fun findSprite(spritePath: String?): Sprite {
        checkNotNull(spritePath)
        return sprites[spritePath] ?: error("spritePath: $spritePath")
    }

    fun removeSpritesheet(spritesheet: Spritesheet) {
        for (spritePath in spritesheet.spritePaths) {
            sprites.remove(spritePath)
        }
    }

    fun clear() {
        spritesheets.clear()
        sprites.clear()
    }

}

/*internals*/

private val textureAtlasPathRegex = "\\.part[0-9]*\\.png".toRegex()
