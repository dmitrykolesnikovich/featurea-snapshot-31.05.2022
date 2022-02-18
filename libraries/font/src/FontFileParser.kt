package featurea.font

import featurea.splitAndTrim
import featurea.splitLines

/*
1. info header
2. common header
3. page header
4. chars header
5. kernings header
6. kernings
7. chars
*/

class FontFileParserException(message: String) : RuntimeException(message)

object FontFileParser {

    // todo make use of `filePath` for debugging purposes
    fun parseFontSource(source: String, filePath: String? = null): Font {
        val lines: List<String> = source.splitLines()

        val fontBuilder: FontBuilder = FontBuilder {
            // 1. info header
            run {
                val info: String = lines[0]
                check(info.startsWith("info ")) { "info: $info" }
                // 1)
                val faceAndTokens: List<String> = info.removePrefix("info ").splitAndTrim("\" ", limit = 2)
                name = faceAndTokens[0].split("=")[1].trim()
                // 2)
                val tokens: List<String> = faceAndTokens[1].split(" ")
                for (token in tokens) {
                    if (token.isBlank()) continue
                    val (key, value) = token.split("=")
                    when (key) {
                        "size" -> size = value.trim().toInt()
                        "bold" -> isBold = value.trim() == "1"
                        "italic" -> isItalic = value.trim() == "1"
                        "padding" -> padding = value.trim().toFontPadding()
                    }
                }
            }

            // 2. common header
            val common: String = lines[1]
            check(common.startsWith("common ")) { "common: $common" }
            val tokens: List<String> = common.removePrefix("common ").split(" ")
            for (token in tokens) {
                if (token.isBlank()) continue
                val (key, value) = token.split("=")
                when (key) {
                    "lineHeight" -> lineHeight = value.trim().toFloat()
                    "base" -> baseY = value.trim().toFloat()
                    "scaleW" -> width = value.trim().toFloat()
                    "scaleH" -> height = value.trim().toFloat()
                }
            }
        }

        // 3. page header
        run {
            val info: String = lines[2]
            check(info.startsWith("page ")) { "page: $info" }
        }

        // 4. chars header
        val charsIndex: Int = 3
        var charCount: Int = -1
        run {
            val chars: String = lines[charsIndex]
            check(chars.startsWith("chars ")) { "chars: $chars" }
            val tokens: List<String> = chars.removePrefix("chars ").split(" ")
            for (token in tokens) {
                if (token.isBlank()) continue
                val (key, value) = token.split("=")
                when (key) {
                    "count" -> charCount = value.trim().toInt()
                }
            }
            check(charCount != -1) { "charCount: $charCount" }
        }

        // 5. kernings header
        val kerningsIndex: Int = charsIndex + charCount + 1
        var kerningsCount: Int = -1
        run {
            val kernings: String = lines[kerningsIndex]
            if (kernings.isNotBlank()) {
                check(kernings.startsWith("kernings ")) { "kernings: $kernings" }
                val tokens: List<String> = kernings.removePrefix("kernings ").split(" ")
                for (token in tokens) {
                    if (token.isBlank()) continue
                    val (key, value) = token.split("=")
                    when (key) {
                        "count" -> kerningsCount = value.trim().toInt()
                    }
                }
                check(kerningsCount != -1) { "kerningsCount: $kerningsCount" }
            }
        }

        // 6. kernings
        val glyphKernings: MutableMap<Int, MutableMap<Int, Float>> = mutableMapOf() // Map<second, <first, amount>>
        if (kerningsCount != -1) {
            val kerningBuilder: KerningBuilder = KerningBuilder()
            for (index in kerningsIndex + 1..kerningsIndex + kerningsCount) {
                val kerning: String = lines[index]
                check(kerning.startsWith("kerning ")) { "kerning: $kerning" }
                kerningBuilder.parse {
                    val tokens: List<String> = kerning.removePrefix("kerning ").split(" ")
                    for (token in tokens) {
                        if (token.isBlank()) continue
                        val (key, value) = token.split("=")
                        when (key) {
                            "first" -> first = value.trim().toInt()
                            "second" -> second = value.trim().toInt()
                            "amount" -> amount = value.trim().toFloat()
                        }
                    }
                }
                val first: Int = kerningBuilder.first ?: throw FontFileParserException("first: null")
                val second: Int = kerningBuilder.second ?: throw FontFileParserException("second: null")
                val amount: Float = kerningBuilder.amount ?: throw FontFileParserException("amount: null")
                val kernings: MutableMap<Int, Float> = glyphKernings.getOrPut(second) { mutableMapOf() }
                kernings[first] = amount
            }
        }

        // 7. chars
        run {
            val glyphBuilder: GlyphBuilder = GlyphBuilder()
            for (index in charsIndex + 1 until kerningsIndex) {
                val char: String = lines[index]
                check(char.startsWith("char ")) { "char: $char" }
                glyphBuilder.parse {
                    val tokens: List<String> = char.removePrefix("char ").split(" ")
                    for (token in tokens) {
                        if (token.isBlank()) continue
                        val (key, value) = token.split("=")
                        when (key) {
                            "id" -> id = value.trim().toInt()
                            "xoffset" -> offsetX = value.trim().toFloat()
                            "yoffset" -> offsetY = value.trim().toFloat()
                            "xadvance" -> advance = value.trim().toFloat()
                            "x" -> x = value.trim().toFloat()
                            "y" -> y = value.trim().toFloat()
                            "width" -> width = value.trim().toFloat()
                            "height" -> height = value.trim().toFloat()
                        }
                    }
                }
                val glyphId: Int = glyphBuilder.id ?: throw FontFileParserException("glyphId: null")
                fontBuilder.glyphs[glyphId] = glyphBuilder.build(glyphKernings[glyphId] ?: emptyMap())
            }
        }

        // result
        return fontBuilder.build()
    }

}

/*internals*/

private class FontBuilder {

    var name: String? = null
    var size: Int? = null
    var isBold: Boolean? = null
    var isItalic: Boolean? = null
    var padding: FontPadding? = null
    var lineHeight: Float? = null
    var baseY: Float? = null
    val glyphs = mutableMapOf<Int, FontGlyph>()
    var width: Float? = null
    var height: Float? = null

    constructor(parse: FontBuilder.() -> Unit) {
        parse()
        validate()
    }

    fun build(): Font {
        val name: String = name ?: throw FontFileParserException("name: null")
        val size: Int = size ?: throw FontFileParserException("size: null")
        val isBold: Boolean = isBold ?: throw FontFileParserException("isBold: null")
        val isItalic: Boolean = isItalic ?: throw FontFileParserException("isItalic: null")
        val padding: FontPadding = padding ?: throw FontFileParserException("padding: null")
        val lineHeight: Float = lineHeight ?: throw FontFileParserException("lineHeight: null")
        val baseY: Float = baseY ?: throw FontFileParserException("baseY: null")
        val width: Float = width ?: throw FontFileParserException("width: null")
        val height: Float = height ?: throw FontFileParserException("height: null")
        return Font(name, size, isBold, isItalic, padding, lineHeight, baseY, glyphs, width, height)
    }

    /*internals*/

    private fun validate() {
        checkNotNull(name)
        checkNotNull(size)
        checkNotNull(isBold)
        checkNotNull(isItalic)
        checkNotNull(padding)
        checkNotNull(lineHeight)
        checkNotNull(baseY)
        checkNotNull(width)
        checkNotNull(height)
    }

}

private class GlyphBuilder {

    var id: Int? = null
    var offsetX: Float? = null
    var offsetY: Float? = null
    var advance: Float? = null
    var x: Float? = null
    var y: Float? = null
    var width: Float? = null
    var height: Float? = null

    fun build(kernings: Map<Int, Float>): FontGlyph {
        val id: Int = id ?: throw FontFileParserException("id: null")
        val offsetX: Float = offsetX ?: throw FontFileParserException("offsetX: null")
        val offsetY: Float = offsetY ?: throw FontFileParserException("offsetY: null")
        val advance: Float = advance ?: throw FontFileParserException("advance: null")
        val x: Float = x ?: throw FontFileParserException("x: null")
        val y: Float = y ?: throw FontFileParserException("y: null")
        val width: Float = width ?: throw FontFileParserException("width: null")
        val height: Float = height ?: throw FontFileParserException("height: null")
        return FontGlyph(id, offsetX, offsetY, advance, x, y, width, height, kernings)
    }

    fun parse(parser: GlyphBuilder.() -> Unit) {
        reset()
        parser()
        validate()
    }

    /*internals*/

    private fun reset() {
        id = null
        offsetX = null
        offsetY = null
        advance = null
        x = null
        y = null
        width = null
        height = null
    }

    private fun validate() {
        checkNotNull(id)
        checkNotNull(offsetX)
        checkNotNull(offsetY)
        checkNotNull(advance)
        checkNotNull(x)
        checkNotNull(y)
        checkNotNull(width)
        checkNotNull(height)
    }

}

private class KerningBuilder {

    var first: Int? = null
    var second: Int? = null
    var amount: Float? = null

    fun parse(parser: KerningBuilder.() -> Unit) {
        reset()
        parser()
        validate()
    }

    /*internals*/

    private fun reset() {
        first = null
        second = null
        amount = null
    }

    private fun validate() {
        checkNotNull(first)
        checkNotNull(second)
        checkNotNull(amount)
    }

}
