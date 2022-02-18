package featurea.font

import featurea.spritesheet.Sprite

class Font(
    val name: String,
    val size: Int,
    val isBold: Boolean,
    val isItalic: Boolean,
    val padding: FontPadding,
    val lineHeight: Float,
    val baseY: Float,
    val glyphs: Map<FontGlyphId, FontGlyph>,
    val width: Float,
    val height: Float
) {

    private lateinit var sprite: Sprite
    val spritePath: String get() = sprite.path

    fun initTextureRegion(textureRegion: Sprite) {
        check(width == textureRegion.width)
        check(height == textureRegion.height)

        this.sprite = textureRegion
        val atlasWidth: Float = textureRegion.spritesheet.size.width
        val atlasHeight: Float = textureRegion.spritesheet.size.height
        val tx: Float = textureRegion.x1
        val ty: Float = textureRegion.y1
        for ((_, glyph) in glyphs) {
            val ox: Float = tx + glyph.x
            val oy: Float = ty + glyph.y
            glyph.u1 = ox / atlasWidth
            glyph.v1 = (atlasHeight - (oy + glyph.height)) / atlasHeight
            glyph.u2 = (ox + glyph.width) / atlasWidth
            glyph.v2 = (atlasHeight - oy) / atlasHeight
        }
    }

}
