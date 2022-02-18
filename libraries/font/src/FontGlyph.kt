package featurea.font

typealias FontGlyphId = Int

class FontGlyph(
    val id: FontGlyphId,
    val offsetX: Float,
    val offsetY: Float,
    val advance: Float,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val kernings: Map<FontGlyphId, Float>
) {
    var u1: Float = -1f
    var v1: Float = -1f
    var u2: Float = -1f
    var v2: Float = -1f
}

fun EmptyGlyph(): FontGlyph = FontGlyph(
    id = 0,
    offsetX = 0f,
    offsetY = 0f,
    advance = 0f,
    x = 0f,
    y = 0f,
    width = 0f,
    height = 0f,
    kernings = emptyMap()
)
