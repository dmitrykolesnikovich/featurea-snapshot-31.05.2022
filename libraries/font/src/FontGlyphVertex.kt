package featurea.font

class FontGlyphVertex(cursorX: Float, cursorY: Float, glyph: FontGlyph) {
    val u1 = glyph.u1
    val v1 = glyph.v1
    val u2 = glyph.u2
    val v2 = glyph.v2
    var x1 = cursorX + glyph.offsetX
    var y1 = cursorY + glyph.offsetY
    var x2 = x1 + glyph.width
    var y2 = y1 + glyph.height
}
