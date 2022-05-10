package featurea.rml.writer

import featurea.Bundle
import featurea.System
import featurea.content.*
import featurea.utils.hasExtension
import featurea.jvm.findFile
import featurea.runtime.Container
import featurea.runtime.Module
import featurea.runtime.import

class RmlWriter(override val module: Module) : ResourceWriterComponent {

    private val content: Content = import()
    private val rmlDeserializer: RmlDeserializer = import()
    private val system: System = import()

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
