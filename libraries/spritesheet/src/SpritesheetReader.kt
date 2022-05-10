package featurea.spritesheet

import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.splitLines
import featurea.text.TextContent

class SpritesheetReader(override val module: Module) : Component {

    private val spriteCache: SpriteCache = import()
    private val textContent: TextContent = import()

    private var isInit: Boolean = false
    private val spritesheetBuilder: SpritesheetBuilder = SpritesheetBuilder()

    suspend fun init(complete: () -> Unit) {
        if (isInit) return
        val source: String = textContent.findText(TEXTURES_PACK_PATH)
        val tokens: List<String> = source.splitLines(2)
        for (token in tokens) {
            if (token.isBlank()) continue
            val lines: List<String> = token.splitLines()
            // 1. atlas
            val spritesheet: Spritesheet = run {
                val name: String = lines[0]
                val texturePackDir: String = TEXTURES_PACK_DIRECTORY_PATH
                val path: String = "$texturePackDir/$name"
                val sizeTokens: List<String> = lines[1].trim().replace("size: ", "").split(",")
                val width: Float = sizeTokens[0].trim().toFloat()
                val height: Float = sizeTokens[1].trim().toFloat()
                Spritesheet(path).apply { size.assign(width, height) }
            }
            spritesheetBuilder.spritesheet = spritesheet

            // 2. sprites
            for (index in 5 until lines.size) {
                val line: String = lines[index]
                if (!line.startsWith(" ")) {
                    spritesheetBuilder.path = line
                } else if (line.startsWith("  xy: ")) {
                    val positionTokens: List<String> = line.replace("  xy: ", "").split(",")
                    spritesheetBuilder.x = positionTokens[0].trim().toFloat()
                    spritesheetBuilder.y = positionTokens[1].trim().toFloat()
                } else if (line.startsWith("  size: ")) {
                    val sizeTokens: List<String> = line.replace("  size: ", "").split(",")
                    spritesheetBuilder.w = sizeTokens[0].trim().toFloat()
                    spritesheetBuilder.h = sizeTokens[1].trim().toFloat()
                }
                if (spritesheetBuilder.hasSprite()) {
                    spritesheet.sprites[spritesheetBuilder.path!!] = spritesheetBuilder.buildSprite()
                    spritesheetBuilder.clear()
                }
            }
            spriteCache.spritesheets[spritesheet.spritePath] = spritesheet
        }
        complete()
        isInit = true
    }

}

/*internals*/

private class SpritesheetBuilder {

    lateinit var spritesheet: Spritesheet
    var path: String? = null
    var x: Float = -1f
    var y: Float = -1f
    var w: Float = -1f
    var h: Float = -1f

    fun clear() {
        path = null
        x = -1f
        y = -1f
        w = -1f
        h = -1f
    }

    fun hasSprite(): Boolean {
        return path != null && x != -1f && y != -1f && w != -1f && h != -1f
    }

    fun buildSprite(): Sprite {
        val path: String = checkNotNull(path)
        val x1: Float = x
        val y1: Float = y
        val x2: Float = x + w
        val y2: Float = y + h
        val u1: Float = x1 / spritesheet.size.width
        val v1: Float = (spritesheet.size.height - y2) / spritesheet.size.height
        val u2: Float = x2 / spritesheet.size.width
        val v2: Float = (spritesheet.size.height - y1) / spritesheet.size.height
        return Sprite(spritesheet, path, x1, y1, x2, y2, u1, v1, u2, v2)
    }

}
