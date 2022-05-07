package featurea.rml.writer

import featurea.Bundle
import featurea.System
import featurea.content.Content
import featurea.content.ResourceTag
import featurea.content.ResourceWriter
import featurea.content.rmlExtensions
import featurea.utils.hasExtension
import featurea.jvm.findFile
import featurea.runtime.Container

class RmlWriter(container: Container) : ResourceWriter {

    private val content: Content = container.import()
    private val rmlDeserializer: RmlDeserializer = container.import()
    private val system: System = container.import()

    override suspend fun write(resourceTag: ResourceTag, key: String, value: String, bundle: Bundle) {
        if (value.hasExtension(system.rmlExtensions)) {
            val rmlFile = system.findFile(value)
            if (rmlFile.exists()) {
                content.providedResources.add(value)
                rmlDeserializer.deserialize(rmlFile.path, bundle)
            }
        }
    }

}
