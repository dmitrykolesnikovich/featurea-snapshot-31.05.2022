package featurea.font.writer

import featurea.*
import featurea.content.Content
import featurea.content.ResourceTag
import featurea.content.ResourceWriter
import featurea.font.reader.FONT_TYPE_PREFIX
import featurea.font.reader.toFontProperties
import featurea.jvm.findFile
import featurea.jvm.userHomePath
import featurea.runtime.Container
import featurea.spritesheet.useTexturePack
import java.io.File

class FontWriter(container: Container) : ResourceWriter {

    private val content: Content = container.import()
    private val system: System = container.import()

    override suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        if (value.startsWith(FONT_TYPE_PREFIX)) {
            // 1.
            val path: String = value.removePrefix(FONT_TYPE_PREFIX)
            val fntPath: String = "$FONT_CACHE_PATH/${path}.fnt"
            val pngPath: String = "$FONT_CACHE_PATH/${path}.png"
            val fontProperties = value.toFontProperties()
            val name: String = fontProperties["name"] as String
            val size: Int = fontProperties["size"] as Int
            val isBold: Boolean = fontProperties["bold"] as Boolean
            val isItalic: Boolean = fontProperties["italic"] as Boolean
            val fontPath: String = "$userHomePath/${fntPath}"
            if (isInstrumentationEnabled && !File(fontPath).exists()) {
                runCommand("createFont '$fontPath' '${name}' $size $isBold $isItalic", name = "Creating Font...")
            }

            // 2.
            content.providedResources.add(value)

            // 3.
            if (system.useTexturePack) {
                bundle.texturePack[pngPath] = system.findFile(pngPath).absolutePath
            }
        }
    }

}
