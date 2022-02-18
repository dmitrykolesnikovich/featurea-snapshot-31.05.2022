package featurea.createFont

import com.badlogic.gdx.tools.hiero.unicodefont.Glyph
import com.badlogic.gdx.tools.hiero.unicodefont.GlyphPage
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont
import featurea.jvm.createNewFileAndDirs
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.awt.Canvas
import java.awt.Font
import java.awt.Font.LAYOUT_LEFT_TO_RIGHT
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.round

private data class Kerning(val first: Int, val second: Int, val amount: Int)

class FontWriter(private val unicodeFont: UnicodeFont) {

    private val canvas = Canvas()
    private val withKerningFont: Font = Font.getFont(mapOf(FONT to unicodeFont.font, KERNING to KERNING_ON))
    private val withKerningRender: FontRenderContext = canvas.getFontMetrics(withKerningFont).fontRenderContext
    private val withoutKerningFont: Font = Font.getFont(mapOf(FONT to unicodeFont.font, KERNING to 0))
    private val withoutKerningRender: FontRenderContext = canvas.getFontMetrics(withoutKerningFont).fontRenderContext

    private fun boundsX(font: Font, fontRenderContext: FontRenderContext, char1: Char, char2: Char): Double {
        val chars = charArrayOf(char1, char2)
        val glyphVector = font.layoutGlyphVector(fontRenderContext, chars, 0, chars.size, LAYOUT_LEFT_TO_RIGHT)
        return glyphVector.getGlyphLogicalBounds(1).bounds2D.x
    }

    private fun withKerningX(char1: Char, char2: Char) = boundsX(withKerningFont, withKerningRender, char1, char2)

    private fun withoutKerningX(char1: Char, char2: Char) = boundsX(withoutKerningFont, withoutKerningRender, char1, char2)

    private fun kerningX(char1: Char, char2: Char): Int = round(withKerningX(char1, char2) - withoutKerningX(char1, char2)).toInt()

    fun writeFont(fntFile: File) {
        unicodeFont.loadGlyphs()

        val nameWithoutExtension = fntFile.nameWithoutExtension
        val face = unicodeFont.font.fontName
        val size = unicodeFont.font.size
        val isBold = unicodeFont.font.isBold
        val isItalic = unicodeFont.font.isItalic
        val pageWidth = unicodeFont.glyphPageWidth
        val pageHeight = unicodeFont.glyphPageHeight
        val glyphPages = unicodeFont.glyphPages as List<GlyphPage>

        fntFile.createNewFileAndDirs()
        PrintStream(FileOutputStream(fntFile)).use { writer ->
            // 1. info
            writer.println("""info face="$face" size=$size bold=${if (isBold) 1 else 0} italic=${if (isItalic) 1 else 0} charset="" unicode=0 stretchH=100 smooth=1 aa=1 padding=${unicodeFont.paddingTop},${unicodeFont.paddingRight},${unicodeFont.paddingBottom},${unicodeFont.paddingLeft} spacing=${unicodeFont.paddingAdvanceX},${unicodeFont.paddingAdvanceY}""".trimIndent())

            // 2. common
            writer.println("""common lineHeight=${unicodeFont.lineHeight} base=${unicodeFont.ascent} scaleW=${pageWidth} scaleH=${pageHeight} pages=${glyphPages.size} packed=0""")

            // 3. page
            run {
                for ((index, _) in glyphPages.withIndex()) {
                    val fileName = if (glyphPages.size == 1) "$nameWithoutExtension.png" else "$nameWithoutExtension${index + 1}.png"
                    writer.println("""page id=$index file="$fileName" """)
                }
            }

            // 4. chars
            writer.println("chars count=${glyphPages.sumBy { it.glyphs.size }}")

            // 5. char
            run {
                for ((index, glyphPage) in glyphPages.withIndex()) {
                    glyphPage.glyphs.sortWith(Comparator { o1, o2 -> o1.codePoint - o2.codePoint })
                    for (glyph in glyphPage.glyphs) {
                        char(writer, pageWidth, pageHeight, index, glyph)
                    }
                }
            }

            // 6. kernings
            // 7. kerning
            run {
                val kernings = mutableListOf<Kerning>()
                for (glyphPage in glyphPages) {
                    for (glyph1 in glyphPage.glyphs) {
                        for (glyph2 in glyphPage.glyphs) {
                            val codePoint1 = glyph1.codePoint
                            val codePoint2 = glyph2.codePoint
                            val amount = kerningX(codePoint1.toChar(), codePoint2.toChar())
                            if (amount != 0) {
                                kernings.add(Kerning(codePoint1, codePoint2, amount))
                            }
                        }
                    }
                }
                writer.println("kernings count=${kernings.size}")
                for (kerning in kernings) {
                    writer.println("kerning first=${kerning.first} second=${kerning.second} amount=${kerning.amount}")
                }
            }
        }

        // 8. pages
        run {
            val intBuffer = createIntBuffer(pageWidth * pageHeight)
            val pageImage = BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB)
            val row = IntArray(pageWidth)
            for ((index, glyphPage) in glyphPages.withIndex()) {
                val fileName = if (glyphPages.size == 1) "$nameWithoutExtension.png" else "$nameWithoutExtension${index + 1}.png"
                glyphPage.texture.bind()
                intBuffer.clear()
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, intBuffer)
                for (y in 0 until pageHeight) {
                    intBuffer.get(row)
                    pageImage.raster.setDataElements(0, y, pageWidth, 1, row)
                }
                val outputFile = File(fntFile.parentFile, fileName)
                ImageIO.write(pageImage, "png", outputFile)
            }
        }
    }

}

private fun char(writer: PrintStream, pageWidth: Int, pageHeight: Int, pageIndex: Int, glyph: Glyph) {
    writer.println("""char id=${String.format("%-7s ", glyph.codePoint)} x=${String.format("%-5s", (glyph.u * pageWidth).toInt())} y=${String.format("%-5s", (glyph.v * pageHeight).toInt())} width=${String.format("%-5s", glyph.width)} height=${String.format("%-5s", glyph.height)} xoffset=${String.format("%-5s", glyph.xOffset)} yoffset=${String.format("%-5s", glyph.yOffset)} xadvance=${String.format("%-5s", glyph.xAdvance)} page=${String.format("%-5s", pageIndex)} chnl=0 """)
}
