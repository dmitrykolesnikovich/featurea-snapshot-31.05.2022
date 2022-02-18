package featurea.font

import featurea.font.HAlign.*
import featurea.font.VAlign.*
import featurea.math.Rectangle

class FontGlyphLayout {

    val bounds: Rectangle = Rectangle()
    var font: Font? = null
    var ha: HAlign = LEFT
    var va: VAlign = TOP
    val lines = mutableListOf<FontGlyphLine>()

    fun update(text: String?, init: FontGlyphLayout.() -> Unit = {}) {
        // 0. reset lines
        lines.clear()
        init()
        var currentLine = FontGlyphLine()
        val text = text ?: return
        val font = font ?: return

        // 1. layout width & height
        var isFirstWord = true
        var cursorX = 0f
        var cursorY = 0f

        fun newLine() {
            cursorX = 0f
            cursorY += font.lineHeight
            currentLine = FontGlyphLine()
            isFirstWord = true
        }

        fun printChar(char: Char) {
            val glyphId = char.toInt()
            val glyph: FontGlyph = font.glyphs[glyphId] ?: EmptyGlyph()
            currentLine.vertices.add(FontGlyphVertex(cursorX, cursorY, glyph))
            cursorX += glyph.advance
        }

        fun printWord(word: String) {
            if (isFirstWord) lines.add(currentLine) else printChar(' ')
            for (char in word) printChar(char)
            isFirstWord = false
        }

        fun hasWrap() = va == WRAP && !isFirstWord

        // 1. print words
        for (lineToken in text.split('\n')) {
            for (wordToken in lineToken.split(' ')) {
                if (hasWrap() && cursorX.checkOutsideBounds(wordToken)) newLine()
                printWord(wordToken)
            }
            newLine()
        }

        // 2. vertical align
        val oy: Float = when (va) {
            TOP, WRAP -> 0f
            MIDDLE -> (bounds.height - height) / 2f
            BOTTOM -> bounds.height - height
        }

        // 3. horizontal align
        for (line in lines) {
            val ox = when (ha) {
                LEFT -> 0f
                CENTER -> (bounds.width - line.right) / 2f
                RIGHT -> bounds.width - line.right
            }
            for (vertex in line.vertices) {
                vertex.x1 += bounds.x1 + ox
                vertex.y1 += bounds.y1 + oy
                vertex.x2 += bounds.x1 + ox
                vertex.y2 += bounds.y1 + oy
            }
        }
    }

    /*internals*/

    private fun Float.checkOutsideBounds(word: String): Boolean {
        val font = font ?: error("font: null")
        var cursorX = this
        for (char in word) {
            val glyph = font.glyphs[char.toInt()] ?: throw IllegalArgumentException()
            cursorX += glyph.offsetX + glyph.width
        }
        return cursorX > bounds.width
    }

}

val FontGlyphLayout.width: Float get() = lines.maxByOrNull { it.width }?.width ?: 0f

val FontGlyphLayout.height: Float get() = lines.size * (font?.lineHeight ?: 0f)

fun FontGlyphLayout.forEachGlyph(action: FontGlyphVertex.() -> Unit) {
    for (line in lines) {
        for (vertex in line.vertices) {
            vertex.action()
        }
    }
}