package featurea.font.content.samples

import featurea.font.content.FontFileParser
import featurea.font.content.samples.Content.arial16Font
import featurea.runtime.import
import featurea.text.TextContent

fun example1() = SampleContext {
    val textContent = import<TextContent>()

    val source = textContent.getText(arial16Font)!!
    val font = FontFileParser.parseFontSource(source, arial16Font)
    check(font.lineHeight == 19f)
    check(font.baseY == 15f)
    check(font.glyphs.size == 159)
}
