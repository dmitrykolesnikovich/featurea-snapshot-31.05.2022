package featurea.font.writer

import featurea.Bundle
import featurea.System
import featurea.content.Content
import featurea.content.ResourceTag
import featurea.content.ResourceWriter
import featurea.font.reader.FONT_TYPE_PREFIX
import featurea.font.reader.toFontProperties
import featurea.image.reader.texturePack
import featurea.jvm.findFile
import featurea.jvm.userHomePath
import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import
import featurea.spritesheet.useTexturePack
import featurea.utils.FONT_CACHE_PATH
import featurea.utils.isInstrumentationEnabled
import featurea.utils.runCommand
import featurea.utils.startsWith
import java.io.File

class FontWriter(override val module: Module) : Component, ResourceWriter {

    private val content: Content = import()
    private val system: System = import()

    override suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        if (value.startsWith(FONT_TYPE_PREFIX)) {
            // 1.
            val path: String = value.removePrefix(FONT_TYPE_PREFIX)
            val fntPath: String = "$FONT_CACHE_PATH/${path}.fnt"
            val pngPath: String = "$FONT_CACHE_PATH/${path}.png"
            val fontProperties: Map<String, Any> = value.toFontProperties()
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
