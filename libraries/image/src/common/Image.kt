package featurea.image

import featurea.opengl.Texture
import featurea.spritesheet.Spritesheet

class Image(val spritesheet: Spritesheet, val texture: Texture) {
    override fun toString(): String = "Image(spritePath=${spritesheet.spritePath})"
}
