package featurea.createFont.examples

import java.awt.Font
import java.awt.font.TextAttribute

fun test2() {
    val textAttributes = HashMap<TextAttribute, Any>()

    textAttributes[TextAttribute.FAMILY] = "Arial"
    textAttributes[TextAttribute.SIZE] = 25f
    textAttributes[TextAttribute.KERNING] = TextAttribute.KERNING_ON

    val font = Font.getFont(textAttributes)
}
