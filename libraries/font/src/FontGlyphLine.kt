package featurea.font

class FontGlyphLine {
    val vertices = mutableListOf<FontGlyphVertex>()
    val left: Float get() = if (vertices.isEmpty()) 0f else vertices.first().x1
    val right: Float get() = if (vertices.isEmpty()) 0f else vertices.last().x2
    val width: Float get() = right - left
}
