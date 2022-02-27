package featurea.createFont

import com.badlogic.gdx.backends.lwjgl.LwjglCanvasContext
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont.RenderType
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ColorEffect
import featurea.math.nextPowerOfTwo
import java.awt.Color
import java.awt.Font
import java.io.File
import kotlin.math.max
import kotlin.math.pow

private const val englishCharacters: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^\$-%+=#_&~*"
private const val russianCharacters: String = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЭЮЯабвгдеёжзийклмнопрстуфхцчшщьыъэюя"
private const val specialCharacters: String = " \u0000\r\t"
private const val fontCharacters: String = "${englishCharacters}${russianCharacters}${specialCharacters}"
private const val padding: Int = 2
private const val spacing: Int = -4
private const val pageWidth: Int = 1024
private fun pageHeightOf(size: Int) = max(256, nextPowerOfTwo((0.08 * ((size + 2 * padding).toDouble().pow(2.0))).toInt()))

class FontCreator {

    private val lock = Object() // https://docs.oracle.com/javase/8/docs/api/java/awt/doc-files/AWTThreadIssues.html

    fun createFont(fntFile: File, name: String, size: Int, isBold: Boolean = false, isItalic: Boolean = false) {
        LwjglCanvasContext {
            val unicodeFont = UnicodeFont(Font.decode(name), size, isBold, isItalic).apply {
                renderType = RenderType.Java
                mono = false
                paddingTop = padding
                paddingRight = padding
                paddingBottom = padding
                paddingLeft = padding
                paddingAdvanceX = spacing
                paddingAdvanceY = spacing
                glyphPageWidth = pageWidth
                glyphPageHeight = pageHeightOf(size)
                effects.add(ColorEffect(Color.white))
                addGlyphs(fontCharacters)
            }
            FontWriter(unicodeFont).writeFont(fntFile)
            synchronized(lock) {
                lock.notify()
            }
        }
        synchronized(lock) {
            lock.wait()
        }
    }

}
