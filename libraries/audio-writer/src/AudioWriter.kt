package featurea.audio.writer

import featurea.Bundle
import featurea.System
import featurea.content.Content
import featurea.content.ResourceTag
import featurea.content.ResourceWriter
import featurea.content.isValidAudioResource
import featurea.jvm.findFile
import featurea.runtime.Component
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import
import java.io.File

class AudioWriter(override val module: Module) : Component, ResourceWriter {

    private val content: Content = import()
    private val system: System = import()

    override suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        if (value.isValidAudioResource()) {
            val mp3File: File = system.findFile(value)
            if (mp3File.exists()) {
                val audioType: String = if (key.endsWith("Sound")) "sound" else "music"
                val resourcePath: String = "${audioType}:/${value}"
                content.providedResources.add(resourcePath)
            }
        }
    }

}
