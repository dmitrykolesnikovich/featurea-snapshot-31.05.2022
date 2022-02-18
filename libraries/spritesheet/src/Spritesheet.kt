package featurea.spritesheet

import featurea.math.Size

class Spritesheet(val spritePath: String) {
    val size: Size = Size()
    val sprites = mutableMapOf<String, Sprite>()
    val spritePaths: Iterable<String> get() = sprites.keys
    override fun toString(): String = "Spritesheet(spritePath=$spritePath)"
}
