package featurea.rml.reader

import featurea.Bundle
import featurea.System
import featurea.content.Resource
import featurea.content.ResourceReaderComponent
import featurea.content.rmlExtensions
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.hasExtension

class RmlReader(override val module: Module) : ResourceReaderComponent {

    private val system: System = import()

    override suspend fun readOrNull(resourcePath: String, bundle: Bundle?): Resource? {
        if (resourcePath.hasExtension(system.rmlExtensions)) {
            return Resource(resourcePath)
        }
        return null
    }

}
